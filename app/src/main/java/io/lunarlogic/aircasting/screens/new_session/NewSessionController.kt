package io.lunarlogic.aircasting.screens.new_session

import android.app.Activity
import android.location.Location
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import io.lunarlogic.aircasting.permissions.PermissionsActivity
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.StartRecordingEvent
import io.lunarlogic.aircasting.exceptions.BluetoothNotSupportedException
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.new_session.choose_location.ChooseLocationViewMvc
import io.lunarlogic.aircasting.screens.new_session.confirmation.ConfirmationViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.*
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceTypeViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_session_type.SelectSessionTypeViewMvc
import io.lunarlogic.aircasting.screens.new_session.session_details.SessionDetailsViewMvc
import io.lunarlogic.aircasting.sensor.SessionBuilder
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector
import io.lunarlogic.aircasting.sensor.microphone.AudioReader
import io.lunarlogic.aircasting.sensor.microphone.MicrophoneReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.*

class NewSessionController(
    private val mContextActivity: AppCompatActivity,
    private val mActivity: PermissionsActivity,
    mViewMvc: NewSessionViewMvc,
    mFragmentManager: FragmentManager,
    private val permissionsManager: PermissionsManager,
    private val bluetoothManager: BluetoothManager,
    private val airBeam2Connector: AirBeam2Connector,
    audioReader: AudioReader,
    private val sessionBuilder: SessionBuilder
) : SelectSessionTypeViewMvc.Listener,
    SelectDeviceTypeViewMvc.Listener,
    SelectDeviceViewMvc.Listener,
    TurnOnAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    ConnectingAirBeamController.Listener,
    AirBeamConnectedViewMvc.Listener,
    SessionDetailsViewMvc.Listener,
    ChooseLocationViewMvc.Listener,
    ConfirmationViewMvc.Listener {

    private val wizardNavigator = NewSessionWizardNavigator(mViewMvc, mFragmentManager)
    private val errorHandler = ErrorHandler(mContextActivity)
    private val sessionsRepository = SessionsRepository()
    private val microphoneReader = MicrophoneReader(audioReader, errorHandler)
    private var sessionType: Session.Type? = null
    private var wifiSSID: String? = null
    private var wifiPassword: String? = null

    fun onStart() {
        wizardNavigator.showFirstStep(this)
    }

    fun onBackPressed() {
        wizardNavigator.onBackPressed()
    }

    override fun onFixedSessionSelected() {
        sessionType = Session.Type.FIXED
        onBluetoothDeviceSelected()
    }
    override fun onMobileSessionSelected() {
        sessionType = Session.Type.MOBILE
        wizardNavigator.goToSelectDeviceType(this)
    }

    override fun onBluetoothDeviceSelected() {
        try {
            if (bluetoothManager.isBluetoothEnabled()) {
                if (bluetoothManager.permissionsGranted()) {
                    LocationHelper.start()
                } else {
                    bluetoothManager.requestBluetoothPermissions()
                }
                wizardNavigator.goToTurnOnAirBeam(this)
                return
            }
        } catch(exception: BluetoothNotSupportedException) {
            errorHandler.showError(exception.messageToDisplay)
        }

        wizardNavigator.goToTurnOnBluetooth(this)
    }

    override fun onMicrophoneDeviceSelected() {
        wizardNavigator.goToSessionDetails(Session.Type.MOBILE, MicrophoneReader.deviceId, this)

        if (mActivity.audioPermissionsGranted(permissionsManager)) {
            startMicrophoneSession()
        } else {
            mActivity.requestAudioPermissions(permissionsManager)
        }
    }

    private fun startMicrophoneSession() {
        microphoneReader.start()
    }

    override fun onTurnOnBluetoothOkClicked() {
        bluetoothManager.enableBluetooth()
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH -> {
                if (bluetoothManager.permissionsGranted(grantResults)) {
                    mActivity.requestBluetoothEnable()
                } else {
                    errorHandler.showError(R.string.errors_bluetooth_required)
                }
            }
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_AUDIO -> {
                if (permissionsManager.permissionsGranted(grantResults)) {
                    startMicrophoneSession()
                } else {
                    errorHandler.showError(R.string.errors_audio_required)
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    LocationHelper.start()
                    wizardNavigator.goToTurnOnAirBeam(this)
                } else {
                    errorHandler.showError(R.string.errors_bluetooth_required)
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onTurnOnAirBeamReadyClicked() {
        wizardNavigator.goToSelectDevice(bluetoothManager, this)
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        val listener = this
        GlobalScope.launch(Dispatchers.Main) {
            var existing: Boolean = false
            val query = GlobalScope.async(Dispatchers.IO) {
                existing = sessionsRepository.alreadyExistsForDeviceId(deviceItem.id)
            }
            query.await()
            if (existing) {
                errorHandler.showError(R.string.active_session_already_exists)
            } else {
                wizardNavigator.goToConnectingAirBeam(deviceItem, listener, airBeam2Connector)
            }
        }
    }

    override fun onConnectionSuccessful(deviceId: String) {
        wizardNavigator.goToAirBeamConnected(deviceId, this)
    }

    override fun onAirBeamConnectedContinueClicked(deviceId: String) {
        wizardNavigator.goToSessionDetails(sessionType!!, deviceId, this)
    }

    override fun validationFailed(errorMessage: String) {
        val toast = Toast.makeText(mContextActivity, errorMessage, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onSessionDetailsContinueClicked(
        deviceId: String,
        sessionType: Session.Type,
        sessionName: String,
        sessionTags: ArrayList<String>,
        indoor: Boolean?,
        streamingMethod: Session.StreamingMethod?,
        wifiSSID: String?,
        wifiPassword: String?
    ) {

        val currentLocation = Session.Location.get(LocationHelper.lastLocation())
        val session = sessionBuilder.build(
            deviceId,
            sessionType,
            sessionName,
            sessionTags,
            Session.Status.NEW,
            indoor,
            streamingMethod,
            currentLocation
        )
        this.wifiSSID = wifiSSID
        this.wifiPassword = wifiPassword

        if (sessionType == Session.Type.MOBILE || indoor == true) {
            wizardNavigator.goToConfirmation(session, this)
        } else {
            wizardNavigator.goToChooseLocation(session, this, errorHandler)
        }
    }

    override fun onContinueClicked(session: Session) {
        wizardNavigator.goToConfirmation(session, this)
    }

    override fun onStartRecordingClicked(session: Session) {
        val event = StartRecordingEvent(session, wifiSSID, wifiPassword)
        EventBus.getDefault().post(event)

        mContextActivity.finish()
    }
}
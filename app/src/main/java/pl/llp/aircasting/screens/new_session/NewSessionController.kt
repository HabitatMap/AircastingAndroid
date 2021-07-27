package pl.llp.aircasting.screens.new_session

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.events.AirBeamConnectionSuccessfulEvent
import pl.llp.aircasting.events.SendSessionAuth
import pl.llp.aircasting.events.StartRecordingEvent
import pl.llp.aircasting.exceptions.BluetoothNotSupportedException
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.ResultCodes
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.lib.areLocationServicesOn
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.location.LocationHelper
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionBuilder
import pl.llp.aircasting.permissions.PermissionsManager
import pl.llp.aircasting.screens.new_session.choose_location.ChooseLocationViewMvc
import pl.llp.aircasting.screens.new_session.confirmation.ConfirmationViewMvc
import pl.llp.aircasting.screens.new_session.connect_airbeam.*
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.screens.new_session.select_device.SelectDeviceTypeViewMvc
import pl.llp.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import pl.llp.aircasting.screens.new_session.session_details.SessionDetailsViewMvc
import pl.llp.aircasting.sensor.AirBeamRecordSessionService
import pl.llp.aircasting.sensor.microphone.MicrophoneDeviceItem
import pl.llp.aircasting.sensor.microphone.MicrophoneService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.permissions.LocationPermissionPopUp
import java.util.*

class NewSessionController(
    private val mContextActivity: AppCompatActivity,
    mViewMvc: NewSessionViewMvc,
    private val mFragmentManager: FragmentManager,
    private val permissionsManager: PermissionsManager,
    private val bluetoothManager: BluetoothManager,
    private val sessionBuilder: SessionBuilder,
    private val settings: Settings,
    private val sessionType: Session.Type
) : SelectDeviceTypeViewMvc.Listener,
    SelectDeviceViewMvc.Listener,
    TurnOnAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    AirBeamConnectedViewMvc.Listener,
    SessionDetailsViewMvc.Listener,
    TurnOnLocationServicesViewMvc.Listener,
    TurnOffLocationServicesViewMvc.Listener,
    ChooseLocationViewMvc.Listener,
    ConfirmationViewMvc.Listener {

    private val wizardNavigator = NewSessionWizardNavigator(mViewMvc, mFragmentManager)
    private val errorHandler = ErrorHandler(mContextActivity)
    private val sessionsRepository = SessionsRepository()
    private var wifiSSID: String? = null
    private var wifiPassword: String? = null

    fun onCreate() {
        EventBus.getDefault().safeRegister(this);
        setupProgressMax()

        if (permissionsManager.locationPermissionsGranted(mContextActivity) || areMapsDisabled()) {
            goToFirstStep()
        } else {
            showLocationPermissionPopUp()
        }
    }

    private fun showLocationPermissionPopUp() {
        LocationPermissionPopUp(mFragmentManager, permissionsManager, mContextActivity).show()
    }

    private fun setupProgressMax() {
        wizardNavigator.setupProgressBarMax(!mContextActivity.areLocationServicesOn(), settings.areMapsDisabled(), !bluetoothManager.isBluetoothEnabled())
    }

    fun onStop() {
        EventBus.getDefault().unregister(this)
    }
    
    private fun goToFirstStep() {
        if (mContextActivity.areLocationServicesOn()) {
            startNewSessionWizard()
        } else {
            wizardNavigator.goToTurnOnLocationServices(this, areMapsDisabled(), sessionType)
        }
    }

    fun onBackPressed() {
        wizardNavigator.onBackPressed()
    }

    private fun startNewSessionWizard() {
        LocationHelper.start()
        when (sessionType) {
            Session.Type.FIXED -> onFixedSessionSelected()
            Session.Type.MOBILE -> onMobileSessionSelected()
        }
    }

    override fun onTurnOnLocationServicesOkClicked() {
        LocationHelper.checkLocationServicesSettings(mContextActivity)
    }

    override fun onTurnOffLocationServicesOkClicked(sessionUUID: String?, deviceItem: DeviceItem?) {
        val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(mContextActivity, intent, null)

        goToSessionDetails(sessionUUID, deviceItem)
    }

    override fun onSkipClicked(sessionUUID: String?, deviceItem: DeviceItem?) {
        goToSessionDetails(sessionUUID, deviceItem)
    }

    private fun requestBluetoothEnable() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        // I know it's deprecated, but location services requires onActivityResult
        // so I wanted to be consistent
        mContextActivity.startActivityForResult(intent, ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE)
    }

    override fun onBluetoothDeviceSelected() {
        try {
            wizardNavigator.progressBarCounter.increaseMaxProgress(4) // 4 additional steps in flow
            if (bluetoothManager.isBluetoothEnabled()) {
                wizardNavigator.goToTurnOnAirBeam(sessionType, this)
                return
            }
        } catch(exception: BluetoothNotSupportedException) {
            errorHandler.showError(exception.messageToDisplay)
        }

        wizardNavigator.goToTurnOnBluetooth(this)
    }

    override fun onMicrophoneDeviceSelected() {
        GlobalScope.launch(Dispatchers.Main) {
            var existing = false
            val query = GlobalScope.async(Dispatchers.IO) {
                existing = sessionsRepository.isMicrophoneSessionAlreadyRecording()
            }
            query.await()
            if (existing) {
                errorHandler.showError(mContextActivity.getString(R.string.you_cant_start_2_microphone_sessions_at_once))
            } else {
                goToCreateMicSession()
            }
        }
    }

    private fun goToCreateMicSession() {
        wizardNavigator.goToSessionDetails(Session.generateUUID(), Session.Type.MOBILE, MicrophoneDeviceItem(), this)

        if (permissionsManager.audioPermissionsGranted(mContextActivity)) {
            startMicrophoneSession()
        } else {
            permissionsManager.requestAudioPermissions(mContextActivity)
        }
    }

    private fun onFixedSessionSelected() {
        onBluetoothDeviceSelected()
    }

    private fun onMobileSessionSelected() {
        wizardNavigator.goToSelectDeviceType(this)
    }

    private fun startMicrophoneSession() {
        MicrophoneService.startService(mContextActivity)
    }

    override fun onTurnOnBluetoothOkClicked() {
        requestBluetoothEnable()
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_LOCATION -> {
                if (permissionsManager.permissionsGranted(grantResults)) {
                    goToFirstStep()
                } else {
                    errorHandler.showError(R.string.errors_location_services_required)
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
            ResultCodes.AIRCASTING_REQUEST_LOCATION_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    startNewSessionWizard()
                } else {
                    errorHandler.showError(R.string.errors_location_services_required)
                }
            }
            ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    wizardNavigator.goToTurnOnAirBeam(sessionType, this)
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

    override fun onConnectClicked(selectedDeviceItem: DeviceItem) {
        GlobalScope.launch(Dispatchers.Main) {
            var existing = false
            val query = GlobalScope.async(Dispatchers.IO) {
                existing = sessionsRepository.mobileSessionAlreadyExistsForDeviceId(selectedDeviceItem.id)
            }
            query.await()
            if (existing) {
                errorHandler.showError(R.string.active_session_already_exists)
            } else {
                connectToAirBeam(selectedDeviceItem)
            }
        }
    }

    private fun connectToAirBeam(deviceItem: DeviceItem) {
        wizardNavigator.goToConnectingAirBeam()
        val sessionUUID = Session.generateUUID()
        AirBeamRecordSessionService.startService(mContextActivity, deviceItem, sessionUUID)
    }

    override fun onAirBeamConnectedContinueClicked(deviceItem: DeviceItem, sessionUUID: String) {
        if (areMapsDisabled() && mContextActivity.areLocationServicesOn() && sessionType == Session.Type.MOBILE) {
            wizardNavigator.goToTurnOffLocationServices(deviceItem, sessionUUID, this)
        } else {
            goToSessionDetails(sessionUUID, deviceItem)
        }
    }

    override fun validationFailed(errorMessage: String) {
        val toast = Toast.makeText(mContextActivity, errorMessage, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onSessionDetailsContinueClicked(
        sessionUUID: String,
        deviceItem: DeviceItem,
        sessionType: Session.Type,
        sessionName: String,
        sessionTags: ArrayList<String>,
        indoor: Boolean,
        streamingMethod: Session.StreamingMethod?,
        wifiSSID: String?,
        wifiPassword: String?
    ) {

        val currentLocation = Session.Location.get(LocationHelper.lastLocation(), areMapsDisabled())

        val session = sessionBuilder.build(
            sessionUUID,
            deviceItem,
            sessionType,
            sessionName,
            sessionTags,
            Session.Status.NEW,
            indoor,
            streamingMethod,
            currentLocation,
            settings
        )

        println("MARYSIA: session device id? New session on session details confirmation ${session.deviceId}")
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
        println("MARYSIA: ne session, on start recoreing clicked, device id ${session.deviceId}")
        val event = StartRecordingEvent(session, wifiSSID, wifiPassword)
        EventBus.getDefault().post(event)
        mContextActivity.setResult(RESULT_OK)
        mContextActivity.finish()
    }

    fun areMapsDisabled(): Boolean {
        return settings.areMapsDisabled()
    }

    fun goToSessionDetails(sessionUUID: String?, deviceItem: DeviceItem?) {
        sessionUUID ?: return
        deviceItem ?: return

        EventBus.getDefault().post(SendSessionAuth(sessionUUID))
        wizardNavigator.goToSessionDetails(sessionUUID, sessionType, deviceItem, this)
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionSuccessfulEvent) {
        val deviceItem = event.deviceItem
        val sessionUUID = event.sessionUUID

        sessionUUID ?: return // it should not happen in the new session wizard flow, but checking just for sure...

        wizardNavigator.goToAirBeamConnected(deviceItem, sessionUUID, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        onBackPressed()
        val header = mContextActivity.resources.getString(R.string.bluetooth_failed_connection_alert_header)
        val description = mContextActivity.resources.getString(R.string.bluetooth_failed_connection_alert_description)
        errorHandler.showErrorDialog(mFragmentManager, header, description)
    }
}

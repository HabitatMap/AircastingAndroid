package io.lunarlogic.aircasting.screens.new_session

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.*
import io.lunarlogic.aircasting.exceptions.BluetoothNotSupportedException
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.areLocationServicesOn
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.new_session.choose_location.ChooseLocationViewMvc
import io.lunarlogic.aircasting.screens.new_session.confirmation.ConfirmationViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.*
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceTypeViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.new_session.session_details.SessionDetailsViewMvc
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionBuilder
import io.lunarlogic.aircasting.screens.common.AircastingAlertDialog
import io.lunarlogic.aircasting.sensor.AirBeamRecordSessionService
import io.lunarlogic.aircasting.sensor.microphone.MicrophoneDeviceItem
import io.lunarlogic.aircasting.sensor.microphone.MicrophoneService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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

        if (permissionsManager.locationPermissionsGranted(mContextActivity) || areMapsDisabled()) {
            goToFirstStep()
        } else {
            permissionsManager.requestLocationPermissions(mContextActivity)
        }
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

        val currentLocation = Session.Location.get(LocationHelper.lastLocation())

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
        val dialog = AircastingAlertDialog(mFragmentManager, mContextActivity.resources.getString(R.string.bluetooth_failed_connection_alert_header), mContextActivity.resources.getString(R.string.bluetooth_failed_connection_alert_description))
        dialog.show()
    }
}

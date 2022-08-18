package pl.llp.aircasting.ui.view.screens.new_session

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.R
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.SessionBuilder
import pl.llp.aircasting.ui.view.screens.new_session.choose_location.ChooseLocationViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.confirmation.ConfirmationViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.*
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.ui.view.screens.new_session.select_device.SelectDeviceViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device_type.SelectDeviceTypeViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsViewMvc
import pl.llp.aircasting.util.ResultCodes
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.util.events.AirBeamConnectionSuccessfulEvent
import pl.llp.aircasting.util.events.SendSessionAuth
import pl.llp.aircasting.util.events.StartRecordingEvent
import pl.llp.aircasting.util.exceptions.BluetoothNotSupportedException
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.adjustMenuVisibility
import pl.llp.aircasting.util.extensions.areLocationServicesOn
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.extensions.showToast
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.permissions.LocationPermissionPopUp
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamRecordSessionService
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneDeviceItem
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneService
import pl.llp.aircasting.util.isSDKGreaterOrEqualToQ

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
        EventBus.getDefault().safeRegister(this)
        setupProgressMax()

        if (permissionsManager.locationPermissionsGranted(mContextActivity) || areMapsDisabled()) goToFirstStep() else showLocationPermissionPopUp()
    }

    private fun showLocationPermissionPopUp() {
        LocationPermissionPopUp(mFragmentManager, permissionsManager, mContextActivity).show()
    }

    private fun setupProgressMax() {
        wizardNavigator.setupProgressBarMax(
            !mContextActivity.areLocationServicesOn(),
            settings.areMapsDisabled(),
            !bluetoothManager.isBluetoothEnabled()
        )
    }

    fun onResume() {
        if (settings.isKeepScreenOnEnabled()) mContextActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mContextActivity.adjustMenuVisibility(false)
    }

    fun onStop() {
        mContextActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

    override fun onTurnOffLocationServicesOkClicked(session: Session?) {
        val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(mContextActivity, intent, null)

        wizardNavigator.goToConfirmation(session, this)
    }

    override fun onSkipClicked(session: Session?) {
        wizardNavigator.goToConfirmation(session, this)
    }

    private fun requestBluetoothEnable() {
        bluetoothManager.requestBluetoothEnable(mContextActivity)
    }

    override fun onBluetoothDeviceSelected() {
        try {
            wizardNavigator.progressBarCounter.increaseMaxProgress(4) // 4 additional steps in flow
            if (bluetoothManager.isBluetoothEnabled()) {
                wizardNavigator.goToTurnOnAirBeam(sessionType, this)
                return
            }
        } catch (exception: BluetoothNotSupportedException) {
            errorHandler.showError(exception.messageToDisplay)
        }

        wizardNavigator.goToTurnOnBluetooth(this)
    }

    @OptIn(DelicateCoroutinesApi::class)
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
        wizardNavigator.goToSessionDetails(
            Session.generateUUID(),
            Session.Type.MOBILE,
            MicrophoneDeviceItem(),
            this
        )

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

    override fun onTurnOnBluetoothContinueClicked() {
        requestBluetoothEnable()
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_LOCATION ->
                if (permissionsManager.permissionsGranted(grantResults)) needAccessBackgroundLocation()
                else {
                    mContextActivity.finish()
                    errorHandler.showError(
                        R.string.errors_location_services_required
                    )
                }

            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                if (!permissionsManager.permissionsGranted(grantResults)) errorHandler.showError(
                        R.string.errors_location_background_services_required
                    )
                goToFirstStep()
            }


            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_AUDIO ->
                if (permissionsManager.permissionsGranted(grantResults)) startMicrophoneSession() else errorHandler.showError(
                    R.string.errors_audio_required
                )

            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH ->
                if (permissionsManager.permissionsGranted(grantResults)) requestBluetoothEnable() else errorHandler.showError(
                    R.string.bluetooth_error_permissions
                )
            else -> {}
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            ResultCodes.AIRCASTING_REQUEST_LOCATION_ENABLE -> if (resultCode == RESULT_OK) startNewSessionWizard()
            else errorHandler.showError(
                R.string.errors_location_services_required
            )

            ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> if (resultCode == RESULT_OK) wizardNavigator.goToTurnOnAirBeam(
                sessionType,
                this
            ) else errorHandler.showError(R.string.errors_bluetooth_required)

            else -> {}
        }
    }

    override fun onTurnOnAirBeamReadyClicked() {
        if (permissionsManager.bluetoothPermissionsGranted(mContextActivity))
            wizardNavigator.goToSelectDevice(
                bluetoothManager,
                this
            ) else permissionsManager.requestBluetoothPermissions(mContextActivity)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onConnectClicked(selectedDeviceItem: DeviceItem) {
        GlobalScope.launch(Dispatchers.Main) {
            var existing = false
            val query = GlobalScope.async(Dispatchers.IO) {
                existing =
                    sessionsRepository.mobileSessionAlreadyExistsForDeviceId(
                        selectedDeviceItem.id
                    )
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

    override fun onAirBeamConnectedContinueClicked(
        deviceItem: DeviceItem,
        sessionUUID: String
    ) {
        goToSessionDetails(sessionUUID, deviceItem)
    }

    override fun validationFailed(errorMessage: String) {
        mContextActivity.showToast(errorMessage, Toast.LENGTH_LONG)
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

        val currentLocation =
            Session.Location.get(LocationHelper.lastLocation(), areMapsDisabled())

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
        if (areMapsDisabled() && mContextActivity.areLocationServicesOn() && sessionType == Session.Type.MOBILE) {
            wizardNavigator.goToTurnOffLocationServices(session, this)
        } else if (sessionType == Session.Type.MOBILE || indoor) {
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

    fun areMapsDisabled()
            : Boolean {
        return settings.areMapsDisabled()
    }

    private fun goToSessionDetails(sessionUUID: String?, deviceItem: DeviceItem?) {
        sessionUUID ?: return
        deviceItem ?: return

        EventBus.getDefault().post(SendSessionAuth(sessionUUID))
        wizardNavigator.goToSessionDetails(sessionUUID, sessionType, deviceItem, this)
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionSuccessfulEvent) {
        val deviceItem = event.deviceItem
        val sessionUUID = event.sessionUUID

        sessionUUID
            ?: return // it should not happen in the new session wizard flow, but checking just for sure...

        wizardNavigator.goToAirBeamConnected(deviceItem, sessionUUID, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        onBackPressed()
        val header =
            mContextActivity.resources.getString(R.string.bluetooth_failed_connection_alert_header)
        val description =
            mContextActivity.resources.getString(R.string.bluetooth_failed_connection_alert_description)
        errorHandler.showErrorDialog(mFragmentManager, header, description)
    }

    private fun needAccessBackgroundLocation() {
        if (isSDKGreaterOrEqualToQ()) permissionsManager.requestBackgroundLocationPermissions(
            mContextActivity
        ) else goToFirstStep()
    }
}

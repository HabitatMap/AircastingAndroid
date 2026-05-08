package pl.llp.aircasting.ui.view.screens.new_session

import android.app.Activity.NOTIFICATION_SERVICE
import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.R
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.AirbeamConnectionStatus
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.SessionBuilder
import pl.llp.aircasting.di.modules.AirbeamConnectionStatusFlow
import pl.llp.aircasting.di.modules.MainScope
import pl.llp.aircasting.ui.view.common.AircastingAlertDialog
import pl.llp.aircasting.ui.view.screens.new_session.choose_location.ChooseLocationViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.confirmation.ConfirmationViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.confirmation.NotificationPermissionDialog
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.AirBeamConnectedViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.TurnOffLocationServicesViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.TurnOnAirBeamViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.TurnOnLocationServicesViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.ui.view.screens.new_session.select_device.SelectDeviceViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device_type.SelectDeviceTypeViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsViewMvc
import pl.llp.aircasting.util.ResultCodes
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.util.events.DisconnectExternalSensorsEvent
import pl.llp.aircasting.util.events.SendSessionAuth
import pl.llp.aircasting.util.events.StartRecordingEvent
import pl.llp.aircasting.util.exceptions.AirBeamMiniV2NackError
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
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneDeviceItem
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneService
import pl.llp.aircasting.util.helpers.sensor.services.AirBeamRecordSessionService
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.FixedSessionConfigureOutcome
import pl.llp.aircasting.util.helpers.sensor.services.BatteryLevelService
import pl.llp.aircasting.util.isSDKGreaterOrEqualToQ

@AssistedFactory
interface NewSessionControllerFactory {
    fun create(
        mContextActivity: AppCompatActivity,
        mViewMvc: NewSessionViewMvc,
        mFragmentManager: FragmentManager,
        sessionType: Session.Type
    ): NewSessionController
}

class NewSessionController @AssistedInject constructor(
    @Assisted private val mContextActivity: AppCompatActivity,
    @Assisted mViewMvc: NewSessionViewMvc,
    @Assisted private val mFragmentManager: FragmentManager,
    @Assisted private val sessionType: Session.Type,
    private val permissionsManager: PermissionsManager,
    private val bluetoothManager: BluetoothManager,
    private val sessionBuilder: SessionBuilder,
    private val settings: Settings,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
    @MainScope
    private val coroutineScope: CoroutineScope,
    @AirbeamConnectionStatusFlow
    private val connectionStatus: StateFlow<AirbeamConnectionStatus?>,
    private val v2StateRepository: AirBeamMiniV2StateRepository,
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

    private val wizardNavigator: NewSessionWizardNavigator =
        NewSessionWizardNavigator(mViewMvc, mFragmentManager)
    private var wifiSSID: String? = null
    private var wifiPassword: String? = null
    private var deviceFirmwareVersion: DeviceItem.FirmwareVersion = DeviceItem.FirmwareVersion.V1
    private var intervalSeconds: Int? = null
    private var fixedConfigureObserverJob: Job? = null

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)
        setupProgressMax()
        observeConnectionStatus()
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
            if (bluetoothManager.isBluetoothEnabled() &&
                permissionsManager.bluetoothPermissionsGranted(mContextActivity)
            ) {
                wizardNavigator.goToTurnOnAirBeam(sessionType, this)
            } else wizardNavigator.goToTurnOnBluetooth(this)
        } catch (exception: BluetoothNotSupportedException) {
            errorHandler.showError(exception.messageToDisplay)
        }
    }

    override fun onMicrophoneDeviceSelected() {
        mContextActivity.lifecycleScope.launch {
            val existing = sessionsRepository.isMicrophoneSessionAlreadyRecording()
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

            else -> errorHandler.showError(R.string.unknown_error)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            ResultCodes.AIRCASTING_REQUEST_LOCATION_ENABLE -> onLocationEnableCheck(resultCode)
            ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> onBluetoothEnableCheck(resultCode)
        }
    }

    private fun onLocationEnableCheck(resultCode: Int) {
        if (resultCode == RESULT_OK) startNewSessionWizard()
        else errorHandler.showError(R.string.errors_location_services_required)
    }

    private fun onBluetoothEnableCheck(resultCode: Int) {
        if (resultCode == RESULT_OK) wizardNavigator.goToSelectDevice(bluetoothManager, this)
        else errorHandler.showError(R.string.errors_bluetooth_required)
    }

    override fun onTurnOnAirBeamReadyClicked() {
        wizardNavigator.goToSelectDevice(bluetoothManager, this)
    }

    override fun onConnectClicked(selectedDeviceItem: DeviceItem) {
        mContextActivity.lifecycleScope.launch {
            val existing =
                sessionsRepository.mobileSessionAlreadyExistsForDeviceId(selectedDeviceItem.id)
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
        wifiPassword: String?,
        intervalSeconds: Int?,
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
        this.deviceFirmwareVersion = deviceItem.firmwareVersion
        this.intervalSeconds = intervalSeconds
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
        when {
            session.type != Session.Type.MOBILE -> checkV2SyncAndStart(session)
            DeviceItem.Type.isBatteryLevelAvailable(session.deviceType) -> checkV2SyncAndHandleBattery(session)
            else -> startRecording(session)
        }
    }

    private fun checkV2SyncAndStart(session: Session) {
        if (v2StateRepository.deviceState == AirBeamMiniV2Configurator.DeviceState.HAS_SAVED_SESSION
            && v2StateRepository.hasSavedMeasurements
        ) {
            SyncBeforeNewV2SessionDialog(
                mFragmentManager,
                onPrepareForSync = { ensureNearbyWifiPermission() },
                onSyncSuccess = { startRecording(session) },
                onSyncFailure = { goBackToDashboard() },
                onSkipSync = { startRecording(session) },
            ).show()
        } else {
            startRecording(session)
        }
    }

    private fun checkV2SyncAndHandleBattery(session: Session) {
        if (v2StateRepository.deviceState == AirBeamMiniV2Configurator.DeviceState.HAS_SAVED_SESSION
            && v2StateRepository.hasSavedMeasurements
        ) {
            SyncBeforeNewV2SessionDialog(
                mFragmentManager,
                onPrepareForSync = { ensureNearbyWifiPermission() },
                onSyncSuccess = { handleBatteryServicePermissionsAndStartRecording(session) },
                onSyncFailure = { goBackToDashboard() },
                onSkipSync = { handleBatteryServicePermissionsAndStartRecording(session) },
            ).show()
        } else {
            handleBatteryServicePermissionsAndStartRecording(session)
        }
    }

    /**
     * Sync failure path from [SyncBeforeNewV2SessionDialog] — abandon the new-session wizard
     * and return to the dashboard without firing [StartRecordingEvent]. AirBeam storage is
     * left intact so the user can retry the sync from the dashboard.
     */
    private fun goBackToDashboard() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
        mContextActivity.setResult(RESULT_OK)
        mContextActivity.finish()
    }

    /**
     * Trigger the runtime grant for `NEARBY_WIFI_DEVICES` (API 33+) before kicking off the
     * V2 manual-sync flow. Without this permission the system Wi-Fi picker driven by
     * `WifiNetworkSpecifier` shows an empty list. Fire-and-forget: if the user denies, the
     * picker will simply fail later and the orchestrator will surface the failure.
     */
    private fun ensureNearbyWifiPermission() {
        if (!permissionsManager.nearbyWifiPermissionGranted(mContextActivity)) {
            permissionsManager.requestNearbyWifiPermission(mContextActivity)
        }
    }

    private fun startRecording(session: Session) {
        if (session.type == Session.Type.MOBILE) settings.increaseActiveMobileSessionsCount()

        val isFixedV2 = session.isFixed() && deviceFirmwareVersion == DeviceItem.FirmwareVersion.V2
        if (isFixedV2) observeFixedConfigureOutcome()

        val event = StartRecordingEvent(session, wifiSSID, wifiPassword, deviceFirmwareVersion, intervalSeconds)
        EventBus.getDefault().post(event)

        if (!isFixedV2) {
            mContextActivity.setResult(RESULT_OK)
            mContextActivity.finish()
        }
    }

    private fun observeFixedConfigureOutcome() {
        wizardNavigator.setConfirmationLoading(true)
        fixedConfigureObserverJob?.cancel()
        fixedConfigureObserverJob = coroutineScope.launch {
            when (val outcome = v2StateRepository.configureOutcome.first()) {
                is FixedSessionConfigureOutcome.Success -> {
                    // Fixed V2 sessions POST measurements over WiFi autonomously, so BLE is no
                    // longer needed once the device confirms the first measurement was sent.
                    EventBus.getDefault().post(DisconnectExternalSensorsEvent())
                    mContextActivity.setResult(RESULT_OK)
                    mContextActivity.finish()
                }

                is FixedSessionConfigureOutcome.Failure -> {
                    wizardNavigator.setConfirmationLoading(false)
                    val header = mContextActivity.getString(R.string.error_dialog_default_header)
                    val message = when (outcome.reason) {
                        FixedSessionConfigureOutcome.Reason.INVALID_WIFI_CREDENTIALS ->
                            mContextActivity.getString(R.string.fixed_session_misconfigured_dialog_description)
                        else ->
                            AirBeamMiniV2NackError(outcome.errorCode).messageToDisplay
                    }
                    AircastingAlertDialog(mFragmentManager, header, message) {
                        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
                        mContextActivity.setResult(RESULT_OK)
                        mContextActivity.finish()
                    }.show()
                }
            }
        }
    }

    private fun handleBatteryServicePermissionsAndStartRecording(session: Session) {
        when {
            areNotificationsEnabled() -> {
                startBatteryLevelService()
                startRecording(session)
            }

            settings.isNotificationDialogDismissed() -> startRecording(session)
            else -> showNotificationPermissionsDialog() { startRecording(session) }
        }
    }

    private fun startBatteryLevelService() {
        settings.setBatteryServiceRestartRequired(true)
        val intent = Intent(mContextActivity.applicationContext, BatteryLevelService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContextActivity.applicationContext.startForegroundService(intent)
        } else {
            mContextActivity.applicationContext.startService(intent)
        }
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

    fun observeConnectionStatus() = coroutineScope.launch {
        connectionStatus.collect {
            it?.let { status ->
                val connected =
                    !status.sessionUUID.isNullOrBlank() && status.deviceItem != null && status.isConnected
                if (connected) onConnected(status.deviceItem!!, status.sessionUUID!!)
            }
        }
    }

    fun onConnected(deviceItem: DeviceItem, sessionUUID: String) {
        if (wizardNavigator.isConnectingAirbeamFragmentVisible()) {
            wizardNavigator.goToAirBeamConnected(deviceItem, sessionUUID, this)
        }
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

    private fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notificationManager =
                mContextActivity.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.areNotificationsEnabled()
        } else true
    }

    private fun openNotificationSettings() {
        val intent = Intent().apply {
            action = "android.settings.APP_NOTIFICATION_SETTINGS"

            // For Android Oreo and later
            putExtra("android.provider.extra.APP_PACKAGE", mContextActivity.packageName)

            // For pre-Android Oreo
            putExtra("app_package", mContextActivity.packageName)
            putExtra("app_uid", mContextActivity.applicationInfo.uid)
        }

        startActivity(mContextActivity, intent, null)
    }

    private fun showNotificationPermissionsDialog(onDecline: () -> Unit) {
        NotificationPermissionDialog(
            mFragmentManager = mFragmentManager,
            okButtonCallback = this::openNotificationSettings,
            dismissButtonCallback = {
                settings.toggleNotificationDialogDismissed()
                onDecline()
            }
        ).show()
    }

}

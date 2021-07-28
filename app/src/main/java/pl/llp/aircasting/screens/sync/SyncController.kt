package pl.llp.aircasting.screens.sync

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.events.sdcard.SDCardClearFinished
import pl.llp.aircasting.events.sdcard.SDCardSyncErrorEvent
import pl.llp.aircasting.events.sessions_sync.SessionsSyncErrorEvent
import pl.llp.aircasting.events.sessions_sync.SessionsSyncSuccessEvent
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.ResultCodes
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.lib.areLocationServicesOn
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.location.LocationHelper
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.networking.services.SessionsSyncService
import pl.llp.aircasting.permissions.PermissionsManager
import pl.llp.aircasting.screens.common.AircastingAlertDialog
import pl.llp.aircasting.screens.new_session.connect_airbeam.TurnOffLocationServicesViewMvc
import pl.llp.aircasting.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import pl.llp.aircasting.screens.new_session.connect_airbeam.TurnOnLocationServicesViewMvc
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import pl.llp.aircasting.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc
import pl.llp.aircasting.screens.sync.error.ErrorViewMvc
import pl.llp.aircasting.screens.sync.refreshed.RefreshedSessionsViewMvc
import pl.llp.aircasting.screens.sync.synced.AirbeamSyncedViewMvc
import pl.llp.aircasting.sensor.AirBeamSyncService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.permissions.LocationPermissionPopUp
import java.util.concurrent.atomic.AtomicBoolean

class SyncController(
    private val mContextActivity: AppCompatActivity,
    mViewMvc: SyncViewMvc,
    private val mPermissionsManager: PermissionsManager,
    private val mBluetoothManager: BluetoothManager,
    private val mFragmentManager: FragmentManager,
    mApiServiceFactory: ApiServiceFactory,
    private val mErrorHandler: ErrorHandler,
    private val mSettings: Settings
):  RefreshedSessionsViewMvc.Listener,
    SelectDeviceViewMvc.Listener,
    RestartAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    TurnOnLocationServicesViewMvc.Listener,
    AirbeamSyncedViewMvc.Listener,
    TurnOffLocationServicesViewMvc.Listener,
    ErrorViewMvc.Listener {

    private val mApiService =  mApiServiceFactory.get(mSettings.getAuthToken()!!)
    private val mSessionsSyncService = SessionsSyncService.get(mApiService, mErrorHandler, mSettings)
    private val mWizardNavigator = SyncWizardNavigator(mContextActivity, mSettings, mViewMvc, mFragmentManager)
    private var mSessionsSyncStarted = AtomicBoolean(false)

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)

        setupProgressBarMax()

        if (mPermissionsManager.locationPermissionsGranted(mContextActivity)) {
            goToRefreshingSessions()
        } else {
            showLocationPermissionPopUp()
        }
    }

    private fun showLocationPermissionPopUp() {
        LocationPermissionPopUp(mFragmentManager, mPermissionsManager, mContextActivity).show()
    }

    private fun setupProgressBarMax() {
        mWizardNavigator.setupProgressBarMax(!mContextActivity.areLocationServicesOn(), mSettings.areMapsDisabled(), !mBluetoothManager.isBluetoothEnabled())
    }

    fun onStop() {
        EventBus.getDefault().unregister(this)
    }

    fun goToRefreshingSessions() {
        mWizardNavigator.goToRefreshingSessions()
        refreshSessionList()
    }

    private fun refreshSessionList() {
        println("MARYSIA: normally sessions sync would be run, commented now")
        mSessionsSyncStarted.set(true)
        EventBus.getDefault().post(SessionsSyncSuccessEvent())
//        mSessionsSyncService.sync(shouldDisplayErrors = false)
    }

    @Subscribe
    fun onMessageEvent(event: SessionsSyncSuccessEvent) {
        if (mSessionsSyncStarted.get()) {
            mSessionsSyncStarted.set(false)
            mWizardNavigator.goToRefreshingSessionsSuccess(this)
        }

    }

    @Subscribe
    fun onMessageEvent(event: SessionsSyncErrorEvent) {
        mWizardNavigator.goToRefreshingSessionsError(this)
    }

    override fun refreshedSessionsContinueClicked() {
        checkLocationServicesSettings()
    }

    override fun refreshedSessionsRetryClicked() {
        onBackPressed()
        refreshSessionList()
    }

    override fun refreshedSessionsCancelClicked() {
        mContextActivity.finish()
    }

    private fun checkLocationServicesSettings() {
        if (mContextActivity.areLocationServicesOn()) {
            if (mBluetoothManager.isBluetoothEnabled()) {
                mWizardNavigator.goToRestartAirBeam(this)
            } else {
                mWizardNavigator.goToTurnOnBluetooth(this)
            }
        } else {
            mWizardNavigator.goToTurnOnLocationServices(this)
        }
    }

    fun onBackPressed() {
        mWizardNavigator.onBackPressed()
    }

    override fun onTurnOnLocationServicesOkClicked() {
        LocationHelper.checkLocationServicesSettings(mContextActivity)
    }

    override fun onTurnOnBluetoothOkClicked() {
        requestBluetoothEnable()
    }

    private fun requestBluetoothEnable() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        // I know it's deprecated, but location services requires onActivityResult
        // so I wanted to be consistent
        mContextActivity.startActivityForResult(intent, ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE)
    }

    override fun onTurnOnAirBeamReadyClicked() {
        mWizardNavigator.goToSelectDevice(mBluetoothManager, this)
    }

    override fun onConnectClicked(deviceItem: DeviceItem) {
        syncAirbeam(deviceItem)
    }

    @Subscribe
    fun onMessageEvent(event: SDCardSyncErrorEvent) {
        val exception = event.exception
        mWizardNavigator.showError(this, exception.messageToDisplay)
    }

    override fun onErrorViewOkClicked() {
        mContextActivity.finish()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        onBackPressed()
        val dialog = AircastingAlertDialog(mFragmentManager, mContextActivity.resources.getString(R.string.bluetooth_failed_connection_alert_header), mContextActivity.resources.getString(R.string.bluetooth_failed_connection_alert_description))
        dialog.show()
    }

    private fun syncAirbeam(deviceItem: DeviceItem) {
        AirBeamSyncService.startService(mContextActivity, deviceItem)
        mWizardNavigator.goToAirbeamSyncing()
    }

    // Sync is finished when data is downloaded from SD card successfully and SD card is cleared
    @Subscribe
    fun onMessageEvent(event: SDCardClearFinished) {
        mWizardNavigator.goToAirbeamSynced(this)
    }

    override fun onAirbeamSyncedContinueClicked() {
        if (mSettings.areMapsDisabled()) {
            mWizardNavigator.goToTurnOffLocationServices(this)
        } else {
            mContextActivity.finish()
        }
    }

    override fun onTurnOffLocationServicesOkClicked(sessionUUID: String?, deviceItem: DeviceItem?) {
        val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        ContextCompat.startActivity(mContextActivity, intent, null)

        mContextActivity.finish()
    }

    override fun onSkipClicked(sessionUUID: String?, deviceItem: DeviceItem?) {
        mContextActivity.finish()
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_LOCATION -> {
                if (mPermissionsManager.permissionsGranted(grantResults)) {
                    goToRefreshingSessions()
                } else {
                    mErrorHandler.showError(R.string.errors_location_services_required)
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
                    if (mBluetoothManager.isBluetoothEnabled()) {
                        mWizardNavigator.goToRestartAirBeam(this)
                    } else {
                        mWizardNavigator.goToTurnOnBluetooth(this)
                    }
                } else {
                    mErrorHandler.showError(R.string.errors_location_services_required)
                }
            }
            ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    mWizardNavigator.goToRestartAirBeam(this)
                } else {
                    mErrorHandler.showError(R.string.errors_bluetooth_required)
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
}

package pl.llp.aircasting.ui.view.screens.sync

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.modules.SyncActiveFlow
import pl.llp.aircasting.ui.view.common.AircastingAlertDialog
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.TurnOffLocationServicesViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.TurnOnLocationServicesViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.ui.view.screens.new_session.select_device.SelectDeviceViewMvc
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc
import pl.llp.aircasting.ui.view.screens.sync.error.ErrorViewMvc
import pl.llp.aircasting.ui.view.screens.sync.refreshed.RefreshedSessionsViewMvc
import pl.llp.aircasting.ui.view.screens.sync.synced.AirbeamSyncedViewMvc
import pl.llp.aircasting.ui.view.screens.sync.syncing.AirbeamSyncingViewMvc
import pl.llp.aircasting.util.ResultCodes
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.util.events.sdcard.SDCardSyncErrorEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardSyncError
import pl.llp.aircasting.util.extensions.areLocationServicesOn
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.services.AirBeamSyncService

@AssistedFactory
interface SyncControllerFactory {
    fun create(
        mContextActivity: AppCompatActivity,
        mViewMvc: SyncViewMvc,
        fragmentManager: FragmentManager
    ): SyncController
}

class SyncController @AssistedInject constructor(
    @Assisted private val mRootActivity: AppCompatActivity,
    @Assisted mViewMvc: SyncViewMvc,
    @Assisted private val mFragmentManager: FragmentManager,
    private val mPermissionsManager: PermissionsManager,
    private val mBluetoothManager: BluetoothManager,
    private val mErrorHandler: ErrorHandler,
    private val mSettings: Settings,
    private val mSessionsSyncService: SessionsSyncService,
    @SyncActiveFlow
    private val syncActiveFlow: MutableSharedFlow<Boolean>,
) : RefreshedSessionsViewMvc.Listener,
    SelectDeviceViewMvc.Listener,
    RestartAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    TurnOnLocationServicesViewMvc.Listener,
    AirbeamSyncedViewMvc.Listener,
    TurnOffLocationServicesViewMvc.Listener,
    AirbeamSyncingViewMvc.Listener,
    ErrorViewMvc.Listener {

    private val mWizardNavigator: SyncWizardNavigator =
        SyncWizardNavigator(mRootActivity, mViewMvc, mFragmentManager, mSettings, mErrorHandler)
    private val wakeLock: PowerManager.WakeLock = (mRootActivity.getSystemService(Context.POWER_SERVICE) as PowerManager)
        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AircastingApplication::SDSyncWakeLock")

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)
        stopReconnectionAttempts()
        setupProgressBarMax()

        if (mPermissionsManager.locationPermissionsGranted(mRootActivity)) {
            goToRefreshingSessions()
        } else {
            showLocationPermissionPopUp()
        }
    }

    private fun stopReconnectionAttempts() = mRootActivity.lifecycleScope.launch {
        syncActiveFlow.emit(true)
    }

    private fun showLocationPermissionPopUp() {
        pl.llp.aircasting.util.helpers.permissions.LocationPermissionPopUp(
            mFragmentManager,
            mPermissionsManager,
            mRootActivity
        ).show()
    }

    private fun setupProgressBarMax() {
        mWizardNavigator.setupProgressBarMax(
            !mRootActivity.areLocationServicesOn(),
            mSettings.areMapsDisabled(),
            !mBluetoothManager.isBluetoothEnabled()
        )
    }

    fun onStop() {
        EventBus.getDefault().unregister(this)
    }

    private fun goToRefreshingSessions() {
        mWizardNavigator.goToRefreshingSessions()
        refreshSessionList()
    }

    private fun refreshSessionList() {
        mRootActivity.lifecycleScope.launch {
            mSessionsSyncService.sync()
                .onSuccess { syncResult ->
                    when (syncResult) {
                        is SessionsSyncService.Result.Success -> {
                            Log.d(this@SyncController.TAG, "Acting on success sync result")
                            goToSuccessScreen()
                        }
                        is SessionsSyncService.Result.Error -> {
                            Log.d(
                                this@SyncController.TAG,
                                "Acting on error sync result: ${syncResult.throwable?.stackTraceToString()}"
                            )
                            goToErrorScreen()
                        }
                    }
                }
                .onFailure {
                    goToErrorScreen()
                }
        }
    }

    private fun goToSuccessScreen() {
        mRootActivity.lifecycleScope.launchWhenResumed {
            mWizardNavigator.goToRefreshingSessionsSuccess(this@SyncController)
        }
    }

    private fun goToErrorScreen() {
        mRootActivity.lifecycleScope.launchWhenResumed {
            mWizardNavigator.goToRefreshingSessionsError(this@SyncController)
        }
    }

    override fun refreshedSessionsContinueClicked() {
        checkLocationServicesSettings()
    }

    override fun refreshedSessionsRetryClicked() {
        onBackPressed()
        refreshSessionList()
    }

    override fun refreshedSessionsCancelClicked() {
        mRootActivity.finish()
    }

    private fun checkLocationServicesSettings() {
        if (mRootActivity.areLocationServicesOn()) {
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
        LocationHelper.checkLocationServicesSettings(mRootActivity)
    }

    override fun onTurnOnBluetoothContinueClicked() {
        requestBluetoothEnable()
    }

    private fun requestBluetoothEnable() {
        mBluetoothManager.requestBluetoothEnable(mRootActivity)
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
        mRootActivity.finish()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        onBackPressed()
        val dialog = AircastingAlertDialog(
            mFragmentManager,
            mRootActivity.resources.getString(R.string.bluetooth_failed_connection_alert_header),
            mRootActivity.resources.getString(R.string.bluetooth_failed_connection_alert_description)
        )
        dialog.show()
    }

    private fun syncAirbeam(deviceItem: DeviceItem) {
        mRootActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        wakeLock.acquire(20 * 60 * 1000L /* 20 minutes */)
        Log.d("WakeLock", "Acquired wakelock: ${wakeLock.isHeld}")

        AirBeamSyncService.startService(mRootActivity, deviceItem)
        mWizardNavigator.goToAirbeamSyncing(this)
    }

    override fun onAirbeamSyncedContinueClicked() {
        if (mSettings.areMapsDisabled()) {
            mWizardNavigator.goToTurnOffLocationServices(this)
        } else {
            mRootActivity.finish()
        }
    }

    override fun onTurnOffLocationServicesOkClicked(session: Session?) {
        val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        ContextCompat.startActivity(mRootActivity, intent, null)

        mRootActivity.finish()
    }

    override fun onSkipClicked(session: Session?) {
        mRootActivity.finish()
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

    override fun syncFinished() {
        mRootActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        wakeLock.release()
        Log.d("WakeLock", "Released wakelock: ${!wakeLock.isHeld}")
        mErrorHandler.handle(SDCardSyncError("syncFinished in syncController"))
        mRootActivity.lifecycleScope.launchWhenResumed {
            syncActiveFlow.emit(false)
            mWizardNavigator.goToAirbeamSynced(this@SyncController)
        }
    }
}

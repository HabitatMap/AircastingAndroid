package io.lunarlogic.aircasting.screens.sync

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.events.AirBeamConnectionFailedEvent
import io.lunarlogic.aircasting.events.sdcard.SDCardClearFinished
import io.lunarlogic.aircasting.events.sdcard.SDCardSyncErrorEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.*
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.common.AircastingAlertDialog
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOffLocationServicesViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnLocationServicesViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc
import io.lunarlogic.aircasting.screens.sync.error.ErrorViewMvc
import io.lunarlogic.aircasting.screens.sync.refreshed.RefreshedSessionsViewMvc
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedViewMvc
import io.lunarlogic.aircasting.sensor.AirBeamSyncService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

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

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)

        if (mPermissionsManager.locationPermissionsGranted(mContextActivity)) {
            goToRefreshingSessions()
        } else {
            mPermissionsManager.requestLocationPermissions(mContextActivity)
        }
    }

    fun onStop() {
        EventBus.getDefault().unregister(this)
    }

    fun goToRefreshingSessions() {
        mWizardNavigator.goToRefreshingSessions()
        refreshSessionList()
    }

    private fun refreshSessionList() {
        mSessionsSyncService?.sync(
            onSuccessCallback = {
                mWizardNavigator.goToRefreshingSessionsSuccess(this)
            },
            onErrorCallack = {
                mWizardNavigator.goToRefreshingSessionsError(this)
            }
        )
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
                        mWizardNavigator.goToSelectDevice(mBluetoothManager, this)
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

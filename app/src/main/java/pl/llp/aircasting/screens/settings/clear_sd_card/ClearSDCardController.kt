package pl.llp.aircasting.screens.settings.clear_sd_card

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.events.sdcard.SDCardClearFinished
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.ResultCodes
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.lib.areLocationServicesOn
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.location.LocationHelper
import pl.llp.aircasting.permissions.PermissionsManager
import pl.llp.aircasting.screens.common.AircastingAlertDialog
import pl.llp.aircasting.screens.new_session.connect_airbeam.TurnOffLocationServicesViewMvc
import pl.llp.aircasting.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import pl.llp.aircasting.screens.new_session.connect_airbeam.TurnOnLocationServicesViewMvc
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import pl.llp.aircasting.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc
import pl.llp.aircasting.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedViewMvc
import pl.llp.aircasting.sensor.AirBeamClearCardService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.permissions.LocationPermissionPopUp

class ClearSDCardController(
    private val mContextActivity: AppCompatActivity,
    mViewMvc: ClearSDCardViewMvc,
    private val mPermissionsManager: PermissionsManager,
    private val mBluetoothManager: BluetoothManager,
    private val mFragmentManager: FragmentManager,
    private val mSettings: Settings
): SelectDeviceViewMvc.Listener,
    RestartAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    TurnOnLocationServicesViewMvc.Listener,
    SDCardClearedViewMvc.Listener,
    TurnOffLocationServicesViewMvc.Listener {
    private val mWizardNavigator =
        ClearSDCardWizardNavigator(
            mContextActivity,
            mSettings,
            mViewMvc,
            mFragmentManager
        )
    private val mErrorHandler = ErrorHandler(mContextActivity)

    fun onCreate() {
        safeRegister(this)

        setupProgressBarMax()

        if (mPermissionsManager.locationPermissionsGranted(mContextActivity)) {
            goToFirstStep()
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

    fun onResume() {
        if (mSettings.isKeepScreenOnEnabled()) mContextActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun onStop() {
        EventBus.getDefault().unregister(this)
    }

    private fun goToFirstStep() {
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

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_LOCATION -> {
                if (mPermissionsManager.permissionsGranted(grantResults)) {
                    goToFirstStep()
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

    override fun onTurnOnAirBeamReadyClicked() {
        mWizardNavigator.goToSelectDevice(mBluetoothManager, this)
    }

    override fun onConnectClicked(selectedDeviceItem: DeviceItem) {
        clearSDCard(selectedDeviceItem)
    }

    private fun clearSDCard(deviceItem: DeviceItem) {
        AirBeamClearCardService.startService(mContextActivity, deviceItem)
        mWizardNavigator.goToClearingSDCard()
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionFailedEvent) {
        onBackPressed()
        val dialog = AircastingAlertDialog(mFragmentManager, mContextActivity.resources.getString(R.string.bluetooth_failed_connection_alert_header), mContextActivity.resources.getString(R.string.bluetooth_failed_connection_alert_description))
        dialog.show()
    }

    @Subscribe
    fun onMessageEvent(event: SDCardClearFinished) {
        mWizardNavigator.goToSDCardCleared(this)
    }

    override fun onSDCardClearedConfirmationClicked() {
        if (mSettings.areMapsDisabled()) {
            mWizardNavigator.goToTurnOffLocationServices(this)
        } else {
            mContextActivity.finish()
        }
    }

    override fun onTurnOffLocationServicesOkClicked(session: Session?) {
        val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        ContextCompat.startActivity(mContextActivity, intent, null)

        mContextActivity.finish()
    }

    override fun onSkipClicked(session: Session?) {
        mContextActivity.finish()
    }
}

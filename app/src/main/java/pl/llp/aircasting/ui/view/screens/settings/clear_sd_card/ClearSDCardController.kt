package pl.llp.aircasting.ui.view.screens.settings.clear_sd_card

import android.app.Activity
import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.AircastingAlertDialog
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.TurnOffLocationServicesViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.TurnOnLocationServicesViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.ui.view.screens.new_session.select_device.SelectDeviceViewMvc
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedViewMvc
import pl.llp.aircasting.util.ResultCodes
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.AirBeamConnectionFailedEvent
import pl.llp.aircasting.util.events.sdcard.SDCardClearFinished
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.areLocationServicesOn
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.services.AirBeamClearCardService

@AssistedFactory
interface ClearSDCardControllerFactory {
    fun create(
        mContextActivity: AppCompatActivity,
        mViewMvc: ClearSDCardViewMvc,
        fragmentManager: FragmentManager
    ): ClearSDCardController
}
class ClearSDCardController @AssistedInject constructor(
    @Assisted private val mContextActivity: AppCompatActivity,
    @Assisted mViewMvc: ClearSDCardViewMvc,
    @Assisted private val mFragmentManager: FragmentManager,
    private val mPermissionsManager: PermissionsManager,
    private val mBluetoothManager: BluetoothManager,
    private val mSettings: Settings,
    private val mErrorHandler: ErrorHandler,
): SelectDeviceViewMvc.Listener,
    RestartAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    TurnOnLocationServicesViewMvc.Listener,
    SDCardClearedViewMvc.Listener,
    TurnOffLocationServicesViewMvc.Listener {
    private val mWizardNavigator =
        ClearSDCardWizardNavigator(
            mContextActivity,
            mViewMvc,
            mFragmentManager,
            mSettings = mSettings
        )

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)

        setupProgressBarMax()

        if (mPermissionsManager.locationPermissionsGranted(mContextActivity)) {
            goToFirstStep()
        } else {
            showLocationPermissionPopUp()
        }
    }

    private fun showLocationPermissionPopUp() {
        pl.llp.aircasting.util.helpers.permissions.LocationPermissionPopUp(
            mFragmentManager,
            mPermissionsManager,
            mContextActivity
        ).show()
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

    override fun onTurnOnBluetoothContinueClicked() {
        requestBluetoothEnable()
    }

    private fun requestBluetoothEnable() {
        mBluetoothManager.requestBluetoothEnable(mContextActivity)
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

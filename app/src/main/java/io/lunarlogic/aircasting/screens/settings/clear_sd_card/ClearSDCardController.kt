package io.lunarlogic.aircasting.screens.settings.clear_sd_card

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.events.AirBeamConnectionFailedEvent
import io.lunarlogic.aircasting.events.sdcard.SDCardClearFinished
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOffLocationServicesViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnLocationServicesViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedViewMvc
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc
import io.lunarlogic.aircasting.sensor.AirBeamClearCardService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

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
    private val mWizardNavigator = ClearSDCardWizardNavigator(mContextActivity, mViewMvc, mFragmentManager)
    private val mErrorHandler = ErrorHandler(mContextActivity)

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)

        if (mPermissionsManager.locationPermissionsGranted(mContextActivity)) {
            goToFirstStep()
        } else {
            mPermissionsManager.requestLocationPermissions(mContextActivity)
        }
    }

    fun onStop() {
        EventBus.getDefault().unregister(this)
    }

    private fun goToFirstStep() {
        if (areLocationServicesOn()) {
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
        // TODO: remove following and handle error
        mWizardNavigator.goToSDCardCleared(this)
    }

    @Subscribe
    fun onMessageEvent(event: SDCardClearFinished) {
        mWizardNavigator.goToSDCardCleared(this)
    }

    override fun onSDCardClearedConfirmationClicked() {
        if (mSettings.areMapsDisabled()) {
            mWizardNavigator.goToTurnOffLocationServices(this)
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

    private fun areLocationServicesOn(): Boolean {
        val manager =
            mContextActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}

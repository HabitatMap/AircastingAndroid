package io.lunarlogic.aircasting.screens.settings.clear_sd_card

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.AirBeamConnectionSuccessfulEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnLocationServicesViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.settings.SDCardCleared.SDCardClearedViewMvc
import io.lunarlogic.aircasting.screens.settings.restart_airbeam.RestartAirBeamViewMvc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ClearSDCardController(
    private val mContextActivity: AppCompatActivity,
    mViewMvc: ClearSDCardViewMvc,
    private val permissionsManager: PermissionsManager,
    private val bluetoothManager: BluetoothManager,
    private val mFragmentManager: FragmentManager
): SelectDeviceViewMvc.Listener,
    RestartAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    TurnOnLocationServicesViewMvc.Listener,
    SDCardClearedViewMvc.Listener {
    private val wizardNavigator = ClearSDCardWizardNavigator(mViewMvc, mFragmentManager)
    private val errorHandler = ErrorHandler(mContextActivity)
    private val sessionsRepository = SessionsRepository()

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)

        if (permissionsManager.locationPermissionsGranted(mContextActivity)) {
            goToFirstStep()
        } else {
            permissionsManager.requestLocationPermissions(mContextActivity)
        }
    }

    fun onStop() {
        EventBus.getDefault().unregister(this)
    }

    private fun goToFirstStep() {
        if (areLocationServicesOn()) {
            if (bluetoothManager.isBluetoothEnabled()) {
                wizardNavigator.goToRestartAirbeam(this)
            } else {
                wizardNavigator.goToTurnOnBluetooth(this)
            }
        } else {
            wizardNavigator.goToTurnOnLocationServices(this)
        }
    }

    fun onBackPressed() {
        wizardNavigator.onBackPressed()
    }

    override fun onTurnOnLocationServicesOkClicked() {
        LocationHelper.checkLocationServicesSettings(mContextActivity)
    }

    private fun requestBluetoothEnable() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        // I know it's deprecated, but location services requires onActivityResult
        // so I wanted to be consistent
        mContextActivity.startActivityForResult(intent, ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE)
    }

    private fun connectToAirBeam(deviceItem: DeviceItem) {
        wizardNavigator.goToClearingSDCard()
        // todo: clearing SD card service
    }

    private fun areLocationServicesOn(): Boolean {
        val manager =
            mContextActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
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
            EventBus.getDefault().post(AirBeamConnectionSuccessfulEvent(DeviceItem(),"0")) //todo: to be changed on SDClearSuccessfullEvent, to be removed <?>
        }
    }

    override fun onTurnOnBluetoothOkClicked() {
        requestBluetoothEnable()
    }

    override fun onSDCardClearedConfirmationClicked() {
        mContextActivity.finish()
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
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            ResultCodes.AIRCASTING_REQUEST_LOCATION_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (bluetoothManager.isBluetoothEnabled()) {
                        wizardNavigator.goToSelectDevice(bluetoothManager, this)
                    } else {
                        wizardNavigator.goToTurnOnBluetooth(this)
                    }
                } else {
                    errorHandler.showError(R.string.errors_location_services_required)
                }
            }
            ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    wizardNavigator.goToRestartAirbeam(this)
                } else {
                    errorHandler.showError(R.string.errors_bluetooth_required)
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    @Subscribe
    fun onMessageEvent(event: AirBeamConnectionSuccessfulEvent) { //todo: to be changed on SD card cleared event
        wizardNavigator.goToSDCardCleared(this)
    }

}

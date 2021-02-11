package io.lunarlogic.aircasting.screens.settings.clearSDCard

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.AirBeamConnectedViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnAirBeamViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnLocationServicesViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.sensor.AirBeamService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class ClearSDCardController(
    private val mContextActivity: AppCompatActivity,
    mViewMvc: ClearSDCardViewMvc,
    private val permissionsManager: PermissionsManager,
    private val bluetoothManager: BluetoothManager,
    private val mFragmentManager: FragmentManager
): ClearSDCardViewMvc.Listener,
    SelectDeviceViewMvc.Listener,
    TurnOnAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    AirBeamConnectedViewMvc.Listener,
    TurnOnLocationServicesViewMvc.Listener {
    private val wizardNavigator = ClearSDCardWizardNavigator(mViewMvc, mFragmentManager)
    private val errorHandler = ErrorHandler(mContextActivity)
    private val sessionsRepository = SessionsRepository()

    fun onCreate() {
        EventBus.getDefault().safeRegister(this);
        goToFirstStep() //todo: in NewSessionController before it there are some conditionals but do i need them here ???
    }

    fun onStop() {
        EventBus.getDefault().unregister(this)
    }

    private fun requestBluetoothEnable() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        // I know it's deprecated, but location services requires onActivityResult
        // so I wanted to be consistent
        mContextActivity.startActivityForResult(intent, ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE)
    }

    private fun connectToAirBeam(deviceItem: DeviceItem) {
        wizardNavigator.goToConnectingAirBeam()
        val sessionUUID = Session.generateUUID()
        AirBeamService.startService(mContextActivity, deviceItem, sessionUUID)
    }

    private fun goToFirstStep() {
        if (areLocationServicesOn()) {
//            startNewSessionWizard() //todo: this one to be changed
        } else {
//            wizardNavigator.goToTurnOnLocationServices(this, areMapsDisabled(), sessionType) //todo: think of what i need here in constr
        }
    }

    private fun areLocationServicesOn(): Boolean {
        val manager =
            mContextActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun onBackPressed() {
        wizardNavigator.onBackPressed()
    }

    override fun onTurnOnLocationServicesOkClicked() {
        LocationHelper.checkLocationServicesSettings(mContextActivity)
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

    override fun onTurnOnAirBeamReadyClicked() {
        wizardNavigator.goToSelectDevice() //todo: is this the screen with list of devices??
    }

    override fun onTurnOnBluetoothOkClicked() {
        requestBluetoothEnable()
    }

    override fun onAirBeamConnectedContinueClicked(deviceItem: DeviceItem, sessionUUID: String) {
//        TODO("Not yet implemented")
        wizardNavigator.goToClearingSDCard()
    }

    fun onConfirmationClicked() {
        // todo: navigate to settings screen- do i need some new interface for this??
    }

}

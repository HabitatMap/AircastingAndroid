package io.lunarlogic.aircasting.screens.new_session

import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import io.lunarlogic.aircasting.bluetooth.BluetoothActivity
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.StartRecordingEvent
import io.lunarlogic.aircasting.exceptions.BluetoothNotSupportedException
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.screens.dashboard.*
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.*

class NewSessionController(
    private val mContextActivity: AppCompatActivity,
    private val mActivity: BluetoothActivity,
    mViewMvc: NewSessionViewMvc,
    mFragmentManager: FragmentManager
) : SelectDeviceTypeViewMvc.Listener,
    SelectDeviceViewMvc.Listener,
    TurnOnAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    ConnectingAirBeamController.Listener,
    AirBeamConnectedViewMvc.Listener,
    SessionDetailsViewMvc.Listener,
    ConfirmationViewMvc.Listener {


    private val wizardNavigator = NewSessionWizardNavigator(mViewMvc, mFragmentManager)
    private val bluetoothManager = BluetoothManager(mActivity)
    private val errorHandler = ErrorHandler(mContextActivity)
    private val sessionsRepository = SessionsRepository()

    fun onStart() {
        wizardNavigator.showFirstStep(this)
    }

    fun onBackPressed() {
        wizardNavigator.onBackPressed()
    }

    override fun onBluetoothDeviceSelected() {
        try {
            if (bluetoothManager.isBluetoothEnabled()) {
                bluetoothManager.requestBluetoothPermissions()
                wizardNavigator.goToTurnOnAirBeam(this)
                return
            }
        } catch(exception: BluetoothNotSupportedException) {
            errorHandler.showError(exception.messageToDisplay)
        }

        wizardNavigator.goToTurnOnBluetooth(this)
    }

    override fun onTurnOnBluetoothOkClicked() {
        bluetoothManager.enableBluetooth()
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH -> {
                if (bluetoothManager.permissionsGranted(grantResults)) {
                    mActivity.requestBluetoothEnable()
                } else {
                    errorHandler.showError(R.string.errors_bluetooth_required)
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    wizardNavigator.goToTurnOnAirBeam(this)
                } else {
                    errorHandler.showError(R.string.errors_bluetooth_required)
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onTurnOnAirBeamReadyClicked() {
        wizardNavigator.goToSelectDevice(bluetoothManager, this)
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        val listener = this
        GlobalScope.launch(Dispatchers.Main) {
            var existing: Boolean = false
            val query = GlobalScope.async(Dispatchers.IO) {
                existing = sessionsRepository.alreadyExistsForDeviceId(deviceItem.id)
            }
            query.await()
            if (existing) {
                errorHandler.showError(R.string.active_session_already_exists)
            } else {
                wizardNavigator.goToConnectingAirBeam(deviceItem, listener)
            }
        }
    }

    override fun onConnectionSuccessful(deviceId: String) {
        wizardNavigator.goToAirBeamConnected(deviceId, this)
    }

    override fun onAirBeamConnectedContinueClicked(deviceId: String) {
        wizardNavigator.goToSessionDetails(deviceId, this)
    }

    override fun validationFailed() {
        val validationError = mContextActivity.getString(R.string.session_name_required)
        val toast = Toast.makeText(mContextActivity, validationError, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onSessionDetailsContinueClicked(deviceId: String, sessionName: String, sessionTags: ArrayList<String>) {
        val session = Session(deviceId, sessionName, sessionTags, Session.Status.NEW)
        wizardNavigator.goToConfirmation(session, this)
    }

    override fun onStartRecordingClicked(session: Session) {
        val event = StartRecordingEvent(session)
        EventBus.getDefault().post(event)

        mContextActivity.finish()
    }
}
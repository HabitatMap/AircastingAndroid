package io.lunarlogic.aircasting.screens.new_session

import android.app.Activity
import android.content.Intent
import android.os.Messenger
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import io.lunarlogic.aircasting.bluetooth.BluetoothActivity
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.exceptions.BluetoothNotSupportedException
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.*
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceFragment

class NewSessionController(
    private val mContextActivity: AppCompatActivity,
    private val mActivity: BluetoothActivity,
    private val mViewMvc: NewSessionViewMvc,
    private val mFragmentManager: FragmentManager,
    private val mMessanger: Messenger
) : SelectDeviceViewMvc.Listener,
    TurnOnAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    ConnectingAirBeamController.Listener {

    val STEP_PROGRESS = 10
    var currentProgressStep = 1
    val bluetoothManager = BluetoothManager(mActivity)
    val errorHandler = ErrorHandler(mContextActivity)

    fun onStart() {
        showFirstStep()
    }

    private fun showFirstStep() {
        replaceFragment(getFirstFragment())
        updateProgressBarView()
    }

    private fun getFirstFragment() : Fragment {
        try {
            if (bluetoothManager.isBluetoothEnabled()) {
                return TurnOnAirBeamFragment(this)
            }
        } catch(exception: BluetoothNotSupportedException) {
            errorHandler.showError(exception.messageToDisplay)
        }

        return TurnOnBluetoothFragment(this)
    }

    fun onBackPressed() {
        decrementStepProgress()
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

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    goToTurnOnAirBeam()
                } else {
                    errorHandler.showError(R.string.errors_bluetooth_required)
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun goToTurnOnAirBeam() {
        incrementStepProgress()
        goToFragment(TurnOnAirBeamFragment(this))
    }

    override fun onTurnOnAirBeamReadyClicked() {
        goToSelectDevice()
    }

    private fun goToSelectDevice() {
        incrementStepProgress()
        goToFragment(SelectDeviceFragment(this, bluetoothManager))
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        goToConnecting(deviceItem)
    }

    private fun goToConnecting(deviceItem: DeviceItem) {
        incrementStepProgress()
        goToFragment(ConnectingAirBeamFragment(deviceItem, this, mMessanger))
    }

    override fun onConnectionSuccessful() {
        mContextActivity.finish()
    }

    private fun incrementStepProgress() {
        currentProgressStep += 1
        updateProgressBarView()
    }

    private fun decrementStepProgress() {
        currentProgressStep -= 1
        updateProgressBarView()
    }

    private fun updateProgressBarView() {
        val prograssBar = mViewMvc.rootView?.findViewById<ProgressBar>(R.id.progress_bar)
        prograssBar?.progress = currentProgressStep * STEP_PROGRESS
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = mFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.new_session_fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun goToFragment(fragment: Fragment) {
        val fragmentTransaction = mFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.new_session_fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}
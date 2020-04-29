package io.lunarlogic.aircasting.screens.new_session

import android.app.Activity
import android.content.Context
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
import io.lunarlogic.aircasting.exceptions.BluetoothPermissionsRequiredException
import io.lunarlogic.aircasting.exceptions.BluetoothRequiredException
import io.lunarlogic.aircasting.exceptions.ExceptionHandler
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
    var currentProgress = STEP_PROGRESS
    val bluetoothManager = BluetoothManager(mActivity)
    val exceptionHandler = ExceptionHandler(mContextActivity)

    fun onStart() {
        setProgressStep(1)
        replaceFragment(TurnOnBluetoothFragment(this))
    }

    fun onBackPressed() {
        setProgress(currentProgress - STEP_PROGRESS)
    }

    override fun onTurnOnBluetoothOkClicked() {
        try {
            bluetoothManager.enableBluetooth()
        } catch(exception: BluetoothNotSupportedException) {
            exceptionHandler.handleAndDisplay(exception)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        try {
            when (requestCode) {
                ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH -> {
                    if (bluetoothManager.permissionsGranted(grantResults)) {
                        mActivity.requestBluetoothEnable()
                    } else {
                        // TODO: change this exception flow
                        throw BluetoothPermissionsRequiredException()
                    }
                }
                else -> {
                    // Ignore all other requests.
                }
            }
        } catch(exception: BluetoothNotSupportedException) {
            exceptionHandler.handleAndDisplay(exception)
        } catch(exception: BluetoothPermissionsRequiredException) {
            exceptionHandler.handleAndDisplay(exception)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            when (requestCode) {
                ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> {
                    if (resultCode == Activity.RESULT_OK) {
                        goToTurnOnAirBeam()
                    }
                }
                else -> {
                    // Ignore all other requests.
                }
            }
        } catch(exception: BluetoothRequiredException) {
            exceptionHandler.handleAndDisplay(exception)
        }
    }

    private fun goToTurnOnAirBeam() {
        setProgressStep(2)
        goToFragment(TurnOnAirBeamFragment(this))
    }

    override fun onTurnOnAirBeamReadyClicked() {
        goToSelectDevice()
    }

    fun goToSelectDevice() {
        setProgressStep(3)
        goToFragment(SelectDeviceFragment(this, bluetoothManager))
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        goToConnecting(deviceItem)
    }

    fun goToConnecting(deviceItem: DeviceItem) {
        setProgressStep(4)
        goToFragment(ConnectingAirBeamFragment(deviceItem, this, mMessanger))
    }

    override fun onConnectionSuccessful() {
        mContextActivity.finish()
    }

    private fun setProgressStep(step: Int) {
        setProgress(step * STEP_PROGRESS)
    }

    private fun setProgress(progress: Int) {
        val prograssBar = mViewMvc.rootView?.findViewById<ProgressBar>(R.id.progress_bar)
        prograssBar?.progress = progress
        currentProgress = progress
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
package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import android.content.Intent
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnAirBeamFragment
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnAirBeamViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnBluetoothFragment
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceFragment
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.items.ADD_NEW_DEVICE_VIEW_TYPE
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import io.lunarlogic.aircasting.bluetooth.BluetoothActivity
import io.lunarlogic.aircasting.bluetooth.BluetoothConnector
import io.lunarlogic.aircasting.bluetooth.BluetoothDeviceFoundReceiver
import io.lunarlogic.aircasting.exceptions.BluetoothNotSupportedException
import io.lunarlogic.aircasting.exceptions.BluetoothPermissionsRequiredException
import io.lunarlogic.aircasting.exceptions.BluetoothRequiredException
import io.lunarlogic.aircasting.exceptions.ExceptionHandler
import io.lunarlogic.aircasting.lib.ResultCodes

class NewSessionController(
    mContext: Context,
    private val mActivity: BluetoothActivity,
    private val mViewMvc: NewSessionViewMvc,
    private val mFragmentManager: FragmentManager
) : SelectDeviceViewMvc.Listener, TurnOnAirBeamViewMvc.Listener, TurnOnBluetoothViewMvc.Listener {

    val STEP_PROGRESS = 10
    var currentProgress = STEP_PROGRESS
    val bluetoothConnector = BluetoothConnector(mActivity)
    val exceptionHandler = ExceptionHandler(mContext)
    val bluetoothDeviceFoundReceiver = BluetoothDeviceFoundReceiver(exceptionHandler)

    fun onCreate() {
        mActivity.registerBluetoothDeviceFoundReceiver(bluetoothDeviceFoundReceiver)
    }

    fun onDestroy() {
        mActivity.unregisterBluetoothDeviceFoundReceiver(bluetoothDeviceFoundReceiver)
    }

    fun onStart() {
        setProgress(1 * STEP_PROGRESS)
        replaceFragment(SelectDeviceFragment(this))
    }

    fun onBackPressed() {
        setProgress(currentProgress - STEP_PROGRESS)
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        setProgress(2 * STEP_PROGRESS)
        when (deviceItem.viewType) {
            ADD_NEW_DEVICE_VIEW_TYPE -> goToTurnOnAirBeam()
        }
    }

    override fun onTurnOnAirBeamReadyClicked() {
        setProgress(3 * STEP_PROGRESS)
        goToTurnOnBluetooth()
    }

    override fun onTurnOnBluetoothReadyClicked() {
        try {
            bluetoothConnector.connect()
        } catch(exception: BluetoothNotSupportedException) {
            exceptionHandler.handleAndDisplay(exception)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        try {
            when (requestCode) {
                ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH -> {
                    bluetoothConnector.onRequestPermissionsResult(grantResults)
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
                    bluetoothConnector.onActivityResult(resultCode)
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
        goToFragment(TurnOnAirBeamFragment(this))
    }

    private fun goToTurnOnBluetooth() {
        goToFragment(TurnOnBluetoothFragment(this))
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
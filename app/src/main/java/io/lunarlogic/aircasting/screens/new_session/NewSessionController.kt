package io.lunarlogic.aircasting.screens.new_session

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import android.content.Intent



val AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH = 7
val AIRCASTING_REQUEST_BLUETOOTH_ENABLE = 8

class NewSessionController(
    private val mActivity: NewSessionActivity?,
    private val mViewMvc: NewSessionViewMvc,
    private val mFragmentManager: FragmentManager
) : SelectDeviceViewMvc.Listener, TurnOnAirBeamViewMvc.Listener, TurnOnBluetoothViewMvc.Listener {

    val STEP_PROGRESS = 10
    var currentProgress = STEP_PROGRESS

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
        val permission1 = ContextCompat.checkSelfPermission(mActivity!!, Manifest.permission.ACCESS_COARSE_LOCATION)
        val permission2 = ContextCompat.checkSelfPermission(mActivity!!, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            println("Bluetooth granting permissions")

            ActivityCompat.requestPermissions(mActivity!!,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH)
        } else {
            println("Bluetooth permissions were already grandted")
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                // TODO: handle that device does not support Bluetooth
            }

            if (bluetoothAdapter?.isEnabled == true) {
                mActivity!!.startDiscovery()
            } else {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                mActivity!!.startActivityForResult(intent, AIRCASTING_REQUEST_BLUETOOTH_ENABLE)
            }
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
package io.lunarlogic.aircasting.bluetooth

import android.app.Activity
import io.lunarlogic.aircasting.exceptions.BluetoothRequiredException
import io.lunarlogic.aircasting.permissions.PermissionsManager

class BluetoothConnector(private val mActivity: BluetoothActivity) {
    val permissionsManager = PermissionsManager()
    val bluetoothManager = BluetoothManager()

    fun connect() {
        if (mActivity.bluetoothPermissionsGranted(permissionsManager)) {
            startDiscovery()
        } else {
            mActivity.requestBluetoothPermissions(permissionsManager)
        }
    }

    fun onRequestPermissionsResult(grantResults: IntArray) {
        if (permissionsManager.permissionsGranted(grantResults)) {
            startDiscovery()
        }
    }

    @Throws(BluetoothRequiredException::class)
    fun onActivityResult(resultCode: Int) {
       if (isBluetoothEnabled(resultCode)) {
           startDiscovery()
       } else {
           throw BluetoothRequiredException()
       }
    }

    fun isBluetoothEnabled(resultCode: Int) : Boolean {
        if (resultCode == Activity.RESULT_OK) {
            return true
        }
        return false
    }

    private fun startDiscovery() {
        if (bluetoothManager.isBluetoothEnabled()) {
            bluetoothManager.startDiscovery()
        }
        else {
            mActivity.requestBluetoothEnable()
        }
    }
}
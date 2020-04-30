package io.lunarlogic.aircasting.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import io.lunarlogic.aircasting.exceptions.BluetoothNotSupportedException
import io.lunarlogic.aircasting.permissions.PermissionsManager

class BluetoothManager(private val mActivity: BluetoothActivity) {
    val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val permissionsManager = PermissionsManager()

    fun pairedDevices(): Set<BluetoothDevice> {
        return adapter?.bondedDevices?: setOf()
    }

    fun enableBluetooth() {
        if (mActivity.bluetoothPermissionsGranted(permissionsManager)) {
            mActivity.requestBluetoothEnable()
        } else {
            mActivity.requestBluetoothPermissions(permissionsManager)
        }
    }

    fun permissionsGranted(grantResults: IntArray) : Boolean {
        return permissionsManager.permissionsGranted(grantResults)
    }

    fun isBluetoothEnabled() : Boolean {
        if (adapter == null) {
            throw BluetoothNotSupportedException()
        }

        return adapter?.isEnabled == true
    }

    fun startDiscovery() {
        adapter?.startDiscovery()
    }

    fun cancelDiscovery() {
        adapter?.cancelDiscovery()
    }
}
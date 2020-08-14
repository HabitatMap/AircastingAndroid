package io.lunarlogic.aircasting.bluetooth

import android.bluetooth.BluetoothAdapter
import io.lunarlogic.aircasting.exceptions.BluetoothNotSupportedException
import io.lunarlogic.aircasting.permissions.PermissionsActivity
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

open class BluetoothManager {
    val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val mActivity: PermissionsActivity
    private val mPermissionsManager: PermissionsManager

    constructor(activity: PermissionsActivity, permissionsManager: PermissionsManager) {
        this.mActivity = activity
        this.mPermissionsManager = permissionsManager
    }

    open fun pairedDeviceItems(): List<DeviceItem> {
        val devices = adapter?.bondedDevices?: setOf()
        return devices.map { DeviceItem(it) }
    }

    fun enableBluetooth() {
        if (permissionsGranted()) {
            mActivity.requestBluetoothEnable()
        } else {
            mActivity.requestBluetoothPermissions(mPermissionsManager)
        }
    }

    open fun requestBluetoothPermissions() {
        mActivity.requestBluetoothPermissions(mPermissionsManager)
    }

    open fun permissionsGranted(): Boolean {
        return mActivity.bluetoothPermissionsGranted(mPermissionsManager)
    }

    fun permissionsGranted(grantResults: IntArray) : Boolean {
        return mPermissionsManager.permissionsGranted(grantResults)
    }

    open fun isBluetoothEnabled() : Boolean {
        if (adapter == null) {
            throw BluetoothNotSupportedException()
        }

        return adapter.isEnabled
    }

    fun startDiscovery() {
        adapter?.startDiscovery()
    }

    fun cancelDiscovery() {
        adapter?.cancelDiscovery()
    }
}

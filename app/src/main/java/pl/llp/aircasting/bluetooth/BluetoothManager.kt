package pl.llp.aircasting.bluetooth

import android.app.Activity
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

interface BluetoothManager {
    fun pairedDeviceItems(): List<DeviceItem>
    fun isBluetoothEnabled(): Boolean
    fun startDiscovery()
    fun cancelDiscovery()
    fun requestBluetoothEnable(activity: Activity?)
}
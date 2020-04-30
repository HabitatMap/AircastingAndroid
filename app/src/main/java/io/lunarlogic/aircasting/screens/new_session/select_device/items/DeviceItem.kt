package io.lunarlogic.aircasting.screens.new_session.select_device.items

import android.bluetooth.BluetoothDevice

class DeviceItem(private val mBluetoothDevice: BluetoothDevice) {
    val name get() = mBluetoothDevice.name?: "Unknown"
    val id get() = mBluetoothDevice.address

    val bluetoothDevice get() = mBluetoothDevice
}

package io.lunarlogic.aircasting.screens.new_session.select_device.items

import android.bluetooth.BluetoothDevice

class DeviceItem(private val mBluetoothDevice: BluetoothDevice) {
    val name: String
    val address: String
    val id: String

    init {
        name = mBluetoothDevice.name?: "Unknown"
        address = mBluetoothDevice.address
        id = mBluetoothDevice.address.replace(":", "")
    }

    val bluetoothDevice get() = mBluetoothDevice
}

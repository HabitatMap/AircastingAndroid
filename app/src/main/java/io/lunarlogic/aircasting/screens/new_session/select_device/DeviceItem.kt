package io.lunarlogic.aircasting.screens.new_session.select_device

import android.bluetooth.BluetoothDevice

open class DeviceItem(private val mBluetoothDevice: BluetoothDevice) {
    open val name: String
    open val address: String
    open val id: String

    init {
        name = mBluetoothDevice.name?: "Unknown"
        address = mBluetoothDevice.address
        id = mBluetoothDevice.address.replace(":", "")
    }

    val bluetoothDevice get() = mBluetoothDevice
}

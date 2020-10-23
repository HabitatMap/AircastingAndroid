package io.lunarlogic.aircasting.screens.new_session.select_device

import android.bluetooth.BluetoothDevice

open class DeviceItem(private val mBluetoothDevice: BluetoothDevice) {
    open val name: String
    open val address: String
    open val id: String

    companion object {
        private val AIRBEAM_NAME_REGEX = "airbeam"
    }

    init {
        name = mBluetoothDevice.name ?: "Unknown"
        address = mBluetoothDevice.address
        id = name.split(":").last()
    }

    val bluetoothDevice get() = mBluetoothDevice

    fun displayName(): String {
        if (isAirBeam()) {
            return name.split(":", "-").first()
        }

        return name
    }

    fun isAirBeam(): Boolean {
        return name.contains(AIRBEAM_NAME_REGEX, true)
    }
}

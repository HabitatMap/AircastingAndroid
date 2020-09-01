package io.lunarlogic.aircasting.bluetooth

import android.bluetooth.BluetoothAdapter
import io.lunarlogic.aircasting.exceptions.BluetoothNotSupportedException
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

open class BluetoothManager {
    val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    open fun pairedDeviceItems(): List<DeviceItem> {
        val devices = adapter?.bondedDevices?: setOf()
        return devices.map { DeviceItem(it) }
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

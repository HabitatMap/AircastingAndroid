package pl.llp.aircasting.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import pl.llp.aircasting.exceptions.BluetoothNotSupportedException
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

@SuppressLint("MissingPermission")
// TODO: Add check for permission.

open class BluetoothManager {
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    open fun pairedDeviceItems(): List<DeviceItem> {
        val devices = adapter?.bondedDevices ?: setOf()
        return devices.map { DeviceItem(it) }
    }

    open fun isBluetoothEnabled(): Boolean {
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

package pl.llp.aircasting.util.helpers.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.exceptions.BluetoothNotSupportedException

@SuppressLint("MissingPermission")
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

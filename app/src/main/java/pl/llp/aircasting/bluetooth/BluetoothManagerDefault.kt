package pl.llp.aircasting.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import pl.llp.aircasting.exceptions.BluetoothNotSupportedException
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

open class BluetoothManagerDefault : BluetoothManager {
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun pairedDeviceItems(): List<DeviceItem> {
        val devices = adapter?.bondedDevices ?: setOf()
        return devices.map { DeviceItem(it) }
    }

    override fun isBluetoothEnabled(): Boolean {
        if (adapter == null) {
            throw BluetoothNotSupportedException()
        }

        return adapter.isEnabled
    }

    override fun startDiscovery() {
        adapter?.startDiscovery()
    }

    override fun cancelDiscovery() {
        adapter?.cancelDiscovery()
    }
}

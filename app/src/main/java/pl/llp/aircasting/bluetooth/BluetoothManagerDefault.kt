package pl.llp.aircasting.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import pl.llp.aircasting.exceptions.BluetoothNotSupportedException
import pl.llp.aircasting.lib.ResultCodes
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

@SuppressLint("MissingPermission")
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

    override fun requestBluetoothEnable(activity: Activity?) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        activity?.startActivityForResult(
            intent,
            ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE
        )
    }
}

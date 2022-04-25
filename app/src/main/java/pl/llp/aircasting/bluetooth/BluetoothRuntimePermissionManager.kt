package pl.llp.aircasting.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.exceptions.BluetoothNotSupportedException
import pl.llp.aircasting.lib.ResultCodes
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

@RequiresApi(Build.VERSION_CODES.S)
open class BluetoothRuntimePermissionManager(
    private val appContext: Context
) : BluetoothManager {
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun pairedDeviceItems(): List<DeviceItem> {
        return if (
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_SCAN
            )
            == PackageManager.PERMISSION_GRANTED
        )
            adapter?.bondedDevices?.map { DeviceItem(it) } ?: listOf()
        else listOf()
    }

    override fun isBluetoothEnabled(): Boolean {
        if (adapter == null) {
            throw BluetoothNotSupportedException()
        }
        return adapter.isEnabled
    }

    override fun startDiscovery() {
        if (
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_SCAN
            )
            == PackageManager.PERMISSION_GRANTED
        )
            adapter?.startDiscovery()
    }

    override fun cancelDiscovery() {
        if (
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_SCAN
            )
            == PackageManager.PERMISSION_GRANTED
        )
            adapter?.cancelDiscovery()
    }

    override fun requestBluetoothEnable(activity: Activity?) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        if (activity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            } == PackageManager.PERMISSION_GRANTED
        ) {
            activity.startActivityForResult(
                intent,
                ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE
            )
        }
    }
}

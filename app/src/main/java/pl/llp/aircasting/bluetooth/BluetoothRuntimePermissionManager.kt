package pl.llp.aircasting.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import pl.llp.aircasting.exceptions.BluetoothNotSupportedException
import pl.llp.aircasting.lib.ResultCodes
import pl.llp.aircasting.screens.main.MainActivity
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
@RequiresApi(Build.VERSION_CODES.S)
open class BluetoothRuntimePermissionManager(
    private val mainActivity: MainActivity
) : BluetoothManager {
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private var devices: MutableSet<BluetoothDevice> = boundDevices()

    private fun boundDevices(): MutableSet<BluetoothDevice> {
        if (ActivityCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH
            )
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        } else {
            return adapter?.bondedDevices ?: mutableSetOf()
        }
        return mutableSetOf()
    }

    override fun pairedDeviceItems(): List<DeviceItem> {
        return if (ActivityCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        )
            devices.map { DeviceItem(it) }
        else listOf()
    }

    override fun isBluetoothEnabled(): Boolean {
        if (adapter == null) {
            throw BluetoothNotSupportedException()
        }

        return adapter.isEnabled
    }

    override fun startDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        )
            adapter?.startDiscovery()
    }

    override fun cancelDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
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

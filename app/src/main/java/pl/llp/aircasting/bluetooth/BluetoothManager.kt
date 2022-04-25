package pl.llp.aircasting.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import pl.llp.aircasting.lib.ResultCodes
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

interface BluetoothManager {
    fun pairedDeviceItems(): List<DeviceItem>
    fun isBluetoothEnabled(): Boolean
    fun startDiscovery()
    fun cancelDiscovery()
    fun requestBluetoothEnable(activity: Activity?)
}
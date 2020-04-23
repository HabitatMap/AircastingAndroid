package io.lunarlogic.aircasting.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.screens.new_session.AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH

class PermissionsManager {
    val BLUETOOTH_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

    fun bluetoothPermissionsGranted(context: Context): Boolean {
        val permissions = BLUETOOTH_PERMISSIONS.map {
            ContextCompat.checkSelfPermission(context, it)
        }

        return permissions.any { it != PackageManager.PERMISSION_GRANTED }
    }

    fun requestBluetoothPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity,
            BLUETOOTH_PERMISSIONS,
            AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH
        )
    }
}
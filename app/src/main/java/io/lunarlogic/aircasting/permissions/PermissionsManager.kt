package io.lunarlogic.aircasting.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.lunarlogic.aircasting.lib.ResultCodes

class PermissionsManager {
    val BLUETOOTH_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    val AUDIO_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)

    fun permissionsGranted(grantResults: IntArray): Boolean {
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            return true
        }
        return false
    }

    fun bluetoothPermissionsGranted(context: Context): Boolean {
        return permissionsGranted(BLUETOOTH_PERMISSIONS, context)
    }

    fun audioPermissionsGranted(context: Context): Boolean {
        return permissionsGranted(AUDIO_PERMISSIONS, context)
    }

    fun requestBluetoothPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity,
            BLUETOOTH_PERMISSIONS,
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH
        )
    }

    fun requestAudioPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity,
            AUDIO_PERMISSIONS,
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_AUDIO
        )
    }

    private fun permissionsGranted(permissionStrings: Array<String>, context: Context): Boolean {
        val permissions = permissionStrings.map {
            ContextCompat.checkSelfPermission(context, it)
        }

        return permissions.all { it == PackageManager.PERMISSION_GRANTED }
    }
}
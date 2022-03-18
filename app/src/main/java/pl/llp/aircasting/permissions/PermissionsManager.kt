package pl.llp.aircasting.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pl.llp.aircasting.lib.ResultCodes

open class PermissionsManager {
    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val LOCATION_BACKGROUND_PERMISSION =
        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    private val AUDIO_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)

    private val BLUETOOTH_PERMISSIONS =
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)

    fun permissionsGranted(grantResults: IntArray): Boolean {
        return (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
    }

    open fun locationPermissionsGranted(context: Context): Boolean {
        return permissionsGranted(LOCATION_PERMISSIONS, context)
    }

    open fun backgroundLocationPermissionsGranted(context: Context): Boolean {
        return permissionsGranted(LOCATION_BACKGROUND_PERMISSION, context)
    }

    open fun audioPermissionsGranted(context: Context): Boolean {
        return permissionsGranted(AUDIO_PERMISSIONS, context)
    }

    open fun bluetoothPermissionsGranted(context: Context): Boolean {
        return permissionsGranted(BLUETOOTH_PERMISSIONS, context)
    }

    open fun requestLocationPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            LOCATION_PERMISSIONS,
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_LOCATION
        )
    }

    open fun requestBackgroundLocationPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            LOCATION_BACKGROUND_PERMISSION,
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
        )
    }

    fun requestAudioPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            AUDIO_PERMISSIONS,
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_AUDIO
        )
    }

    fun requestBluetoothPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            BLUETOOTH_PERMISSIONS,
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH
        )
    }

    private fun permissionsGranted(permissionStrings: Array<String>, context: Context): Boolean {
        val permissions = permissionStrings.map {
            ContextCompat.checkSelfPermission(context, it)
        }

        return permissions.all {
            it == PackageManager.PERMISSION_GRANTED
        }
    }
}
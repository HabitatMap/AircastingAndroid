package pl.llp.aircasting.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pl.llp.aircasting.lib.ResultCodes

open class PermissionsManager {
    private val LOCATION_PERMISSIONS = if (Build.VERSION.SDK_INT >= 29) arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )
    else
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    private val AUDIO_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)

    fun permissionsGranted(grantResults: IntArray): Boolean {
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            return true
        }
        return false
    }

    open fun locationPermissionsGranted(context: Context): Boolean {
        return permissionsGranted(LOCATION_PERMISSIONS, context)
    }

    open fun audioPermissionsGranted(context: Context): Boolean {
        return permissionsGranted(AUDIO_PERMISSIONS, context)
    }

    open fun requestLocationPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            LOCATION_PERMISSIONS,
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_LOCATION
        )
    }

    fun requestAudioPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            AUDIO_PERMISSIONS,
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_AUDIO
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

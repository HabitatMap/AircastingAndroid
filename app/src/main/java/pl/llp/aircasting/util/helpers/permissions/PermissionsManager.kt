package pl.llp.aircasting.util.helpers.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pl.llp.aircasting.util.ResultCodes
import pl.llp.aircasting.util.isSDKGreaterOrEqualToS

open class PermissionsManager {
    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val LOCATION_BACKGROUND_PERMISSION =
        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    private val AUDIO_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)

    private val BLUETOOTH_PERMISSIONS =
        if (isSDKGreaterOrEqualToS()) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

    private val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)

    /**
     * Required by `WifiNetworkSpecifier` on Android 13+ (API 33). Without it the system
     * Wi-Fi picker silently shows an empty list when the V2 manual-sync flow asks the user
     * to join the AirBeam SoftAP.
     */
    private val NEARBY_WIFI_PERMISSION =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            emptyArray()
        }

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

    open fun cameraPermissionGranted(context: Context): Boolean {
        return permissionsGranted(CAMERA_PERMISSION, context)
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

    fun requestCameraPermission(activity: Activity){
        ActivityCompat.requestPermissions(
            activity,
            CAMERA_PERMISSION,
            ResultCodes.AIRCASTING_PERMISSION_REQUEST_CAMERA
        )
    }

    open fun nearbyWifiPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return permissionsGranted(NEARBY_WIFI_PERMISSION, context)
    }

    fun requestNearbyWifiPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        ActivityCompat.requestPermissions(
            activity,
            NEARBY_WIFI_PERMISSION,
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_NEARBY_WIFI
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
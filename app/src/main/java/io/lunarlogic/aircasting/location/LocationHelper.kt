package io.lunarlogic.aircasting.location

import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import io.lunarlogic.aircasting.events.LocationChanged
import io.lunarlogic.aircasting.lib.ResultCodes
import org.greenrobot.eventbus.EventBus


class LocationHelper(private val mContext: Context) {
    companion object {
        private lateinit var singleton: LocationHelper
        private var started = false

        fun setup(context: Context) {
            singleton = LocationHelper(context)
        }

        fun turnOnLocationServices(activity: AppCompatActivity) {
            singleton.turnOnLocationServices(activity)
        }

        fun start(callback: (() -> Unit)? = null) {
            if (!started) {
                singleton.start(callback)
            } else {
                callback?.invoke()
            }
            started = true
        }

        fun stop() {
            singleton.stop()
        }

        fun lastLocation(): Location? {
            return singleton.lastLocation
        }
    }

    private var mLastLocation: Location? = null
    private val lastLocation get() = mLastLocation

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)

    private val locationRequest: LocationRequest

    init {
        locationRequest = createLocationRequest()
    }

    private var locationCallback: LocationCallback? = null

    fun turnOnLocationServices(activity: AppCompatActivity) {
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .setAlwaysShow(true)
            .addLocationRequest(locationRequest)
            .build()

        val settingsClient = LocationServices.getSettingsClient(activity)

        val task = settingsClient.checkLocationSettings(locationSettingsRequest)
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(activity,
                        ResultCodes.AIRCASTING_REQUEST_LOCATION_ENABLE)
                } catch (sendEx: IntentSender.SendIntentException) { }
            }
        }
    }

    fun start(callback: (() -> Unit)? = null) {
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    mLastLocation = location
                }

                callback?.invoke()

                EventBus.getDefault().post(LocationChanged(mLastLocation?.latitude, mLastLocation?.longitude))
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stop() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }
}

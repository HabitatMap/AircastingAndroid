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

        fun setup(context: Context) {
            singleton = LocationHelper(context)
        }

        fun turnOnLocationServices(activity: AppCompatActivity) {
            singleton.turnOnLocationServices(activity)
        }

        fun start() {
            singleton.start()
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

    private var locationRequest: LocationRequest? = null

    private var locationCallback: LocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                mLastLocation = location
            }

            EventBus.getDefault().post(LocationChanged(mLastLocation?.latitude, mLastLocation?.longitude))
        }
    }

    fun turnOnLocationServices(activity: AppCompatActivity) {
        if (locationRequest != null) return

        locationRequest = createLocationRequest()

        val builder = LocationSettingsRequest.Builder()
        builder.setAlwaysShow(true)
        builder.addLocationRequest(locationRequest!!)
        val locationSettingsRequest = builder.build()

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

    fun start() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stop() {
        locationRequest = null
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }
}

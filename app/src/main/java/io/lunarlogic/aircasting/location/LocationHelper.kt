package io.lunarlogic.aircasting.location

import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*

class LocationHelper(private val mContext: Context) {
    companion object {
        private lateinit var singleton: LocationHelper

        fun setup(context: Context) {
            singleton = LocationHelper(context)
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
        }
    }

    fun start() {
        if (locationRequest != null) return

        locationRequest = createLocationRequest()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stop() {
        locationRequest = null
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest(): LocationRequest {
        println("ANIA createLocationRequest")
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }
}
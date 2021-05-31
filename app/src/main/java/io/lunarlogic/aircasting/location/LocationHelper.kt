package io.lunarlogic.aircasting.location

import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import io.lunarlogic.aircasting.events.LocationChanged
import io.lunarlogic.aircasting.lib.ResultCodes
import org.greenrobot.eventbus.EventBus


class LocationHelper(private val mContext: Context) {
    companion object {
        private var singleton: LocationHelper? = null
        private var started = false

        fun setup(context: Context) {
            if (singleton == null) singleton = LocationHelper(context)
            Log.i("SESS_MAN", "singleton setup, singleton: "+ singleton.toString())
        }

        fun checkLocationServicesSettings(activity: AppCompatActivity) {
            singleton?.checkLocationServicesSettings(activity)
        }

        fun start() {
            if (!started) {
                singleton?.start()
                Log.i("SESS_MAN", "start singleton (in if): " + singleton.toString())
            }
            started = true
            Log.i("SESS_MAN", "start singleton (after if): " + singleton.toString())
        }

        fun stop() {
            singleton?.stop()
        }

        fun lastLocation(): Location? {
            return singleton?.lastLocation
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

    fun checkLocationServicesSettings(activity: AppCompatActivity) {
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .setAlwaysShow(true)
            .addLocationRequest(locationRequest)
            .build()

        val settingsClient = LocationServices.getSettingsClient(activity)

        val task = settingsClient.checkLocationSettings(locationSettingsRequest)
        task.addOnSuccessListener {
            start()
        }
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
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    mLastLocation = location
                }

                EventBus.getDefault().post(LocationChanged(mLastLocation?.latitude, mLastLocation?.longitude))
            }
        }
        Log.i("SESS_MAN", "locationCallback: "+ locationCallback.toString())

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stop() {
        started = false

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

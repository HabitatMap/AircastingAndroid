package pl.llp.aircasting.util.helpers.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.util.ResultCodes
import pl.llp.aircasting.util.events.LocationChanged

class LocationHelper(mContext: Context) {
    companion object {
        private var singleton: LocationHelper? = null
        private var started = false

        fun setup(context: Context) {
            if (singleton == null) singleton = LocationHelper(context)
        }

        fun checkLocationServicesSettings(activity: AppCompatActivity) {
            singleton?.checkLocationServicesSettings(activity)
        }

        fun start() {
            if (!started) {
                singleton?.start()
            }
            started = true
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
                    e.startResolutionForResult(
                        activity,
                        ResultCodes.AIRCASTING_REQUEST_LOCATION_ENABLE
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", sendEx.message.toString())
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult
                for (location in locationResult.locations) {
                    mLastLocation = location
                }

                EventBus.getDefault()
                    .post(LocationChanged(mLastLocation?.latitude, mLastLocation?.longitude))
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback as LocationCallback, Looper.getMainLooper())

    }

    fun stop() {
        started = false

        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
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
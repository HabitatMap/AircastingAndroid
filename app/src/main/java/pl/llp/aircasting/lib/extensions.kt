package pl.llp.aircasting.lib

import android.content.Context
import android.location.LocationManager
import org.greenrobot.eventbus.EventBus

fun EventBus.safeRegister(subscriber: Any) {
    if (!EventBus.getDefault().isRegistered(subscriber)) {
        EventBus.getDefault().register(subscriber)
    }
}

fun Context.areLocationServicesOn(): Boolean {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    return manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun labelFormat(value: Float?): String {
    return "%d".format(value?.toInt())
}
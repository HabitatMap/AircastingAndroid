package pl.llp.aircasting.lib

import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R

fun EventBus.safeRegister(subscriber: Any) {
    if (!EventBus.getDefault().isRegistered(subscriber)) {
        EventBus.getDefault().register(subscriber)
    }
}

fun Context.areLocationServicesOn(): Boolean {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    return manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun styleGoogleMap(map: GoogleMap, context: Context) {
    map.setMapStyle(
        MapStyleOptions.loadRawResourceStyle(
            context, R.raw.map_style
        )
    )
}

fun labelFormat(value: Float?): String {
    return "%d".format(value?.toInt())
}

fun TextView.setAppearance(context: Context, res: Int) {
    if (Build.VERSION.SDK_INT < 23) {
        setTextAppearance(context, res)
    } else {
        setTextAppearance(res)
    }
}
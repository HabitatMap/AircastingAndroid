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

/**
 * Converting Extensions
 * Here we have different extensions for converting from Fahrenheit to Celsius and Vise versa.
 * It accepts Double, Float, Int.
 */

fun temperatureFromFahrenheitToCelsius(fahrenheitTemperature: Double): Double {
    return ((fahrenheitTemperature - 32) * 5) / 9
}

fun temperatureFromFahrenheitToCelsius(fahrenheitTemperature: Float): Float {
    return ((fahrenheitTemperature - 32) * 5) / 9
}

fun temperatureFromFahrenheitToCelsius(fahrenheitTemperature: Int): Int {
    return ((fahrenheitTemperature - 32) * 5) / 9
}

fun temperatureFromCelsiusToFahrenheit(fahrenheitTemperature: Int): Int {
    return fahrenheitTemperature * 9 / 5 + 32
}

fun temperatureFromCelsiusToFahrenheit(fahrenheitTemperature: Double): Double {
    return fahrenheitTemperature * 9 / 5 + 32
}

fun labelFormat(value: Float?): String {
    return "%d".format(value?.toInt())
}
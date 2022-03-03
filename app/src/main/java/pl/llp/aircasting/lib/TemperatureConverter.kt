package pl.llp.aircasting.lib

import pl.llp.aircasting.models.MeasurementStream

class TemperatureConverter private constructor(settings: Settings) {
    companion object {

        private var singleton: TemperatureConverter? = null

        fun setup(settings: Settings) {
            if (singleton == null) singleton = TemperatureConverter(settings)
        }

        fun get(): TemperatureConverter? {
            return singleton
        }

        fun setAppropriateDetailedType(stream: MeasurementStream) {
            if (isCelsiusToggleEnabled()) stream.detailedType = "C"
        }

        fun isCelsiusToggleEnabled(): Boolean {
            return singleton?.mSettings?.isCelsiusScaleEnabled() == true
        }

        fun fahrenheitToCelsius(fahrenheitTemperature: Double): Double {
            return (Math.round(((fahrenheitTemperature - 32) * 5) / 9)).toDouble()
        }

        fun fahrenheitToCelsius(fahrenheitTemperature: Float): Float {
            return (Math.round(((fahrenheitTemperature - 32) * 5) / 9)).toFloat()
        }

        fun celsiusToFahrenheit(fahrenheitTemperature: Int): Int {
            return fahrenheitTemperature * 9 / 5 + 32
        }

        fun celsiusToFahrenheit(fahrenheitTemperature: Double): Double {
            return (Math.round(fahrenheitTemperature * 9 / 5 + 32)).toDouble()
        }
    }

    private var mSettings: Settings? = settings
}

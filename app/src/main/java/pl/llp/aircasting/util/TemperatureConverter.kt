package pl.llp.aircasting.util

import pl.llp.aircasting.data.model.MeasurementStream

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

        fun fahrenheitToCelsius(fahrenheit: Double): Double {
            return (Math.round(((fahrenheit - 32) * 5) / 9)).toDouble()
        }

        fun fahrenheitToCelsius(fahrenheit: Float): Float {
            return fahrenheitToCelsius(fahrenheit.toDouble()).toFloat()
        }

        fun celsiusToFahrenheit(celsius: Double): Double {
            return (Math.round(celsius * 9 / 5 + 32)).toDouble()
        }

        fun celsiusToFahrenheit(celsius: Int): Int {
            return celsiusToFahrenheit(celsius.toDouble()).toInt()
        }

    }

    private var mSettings: Settings? = settings
}

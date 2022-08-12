package pl.llp.aircasting.util

import pl.llp.aircasting.data.model.MeasurementStream

class TemperatureConverter private constructor(settings: Settings) {
    companion object {
        private const val fallbackValue = -9999.0

        private var singleton: TemperatureConverter? = null

        fun setup(settings: Settings) {
            if (singleton == null) singleton = TemperatureConverter(settings)
        }

        fun get(): TemperatureConverter? {
            return singleton
        }

        fun setAppropriateDetailedTypeAndUnitSymbol(stream: MeasurementStream) {
            if (isCelsiusToggleEnabled()) {
                stream.detailedType = "C"
                stream.unitSymbol = "C"
            }
        }

        fun isCelsiusToggleEnabled(): Boolean {
            return singleton?.mSettings?.isCelsiusScaleEnabled() == true
        }

        fun fahrenheitToCelsius(fahrenheit: Double?): Double {
            return if (fahrenheit != null)
                (Math.round(((fahrenheit - 32) * 5) / 9)).toDouble()
            else fallbackValue
        }

        fun fahrenheitToCelsius(fahrenheit: Float?): Float {
            return if (fahrenheit != null)
                fahrenheitToCelsius(fahrenheit.toDouble()).toFloat()
            else fallbackValue.toFloat()
        }

        fun celsiusToFahrenheit(celsius: Double?): Double {
            return if (celsius != null)
                (Math.round(celsius * 9 / 5 + 32)).toDouble()
            else fallbackValue
        }

        fun celsiusToFahrenheit(celsius: Int?): Int {
            return if (celsius != null)
                celsiusToFahrenheit(celsius.toDouble()).toInt()
            else fallbackValue.toInt()
        }

    }

    private var mSettings: Settings? = settings
}

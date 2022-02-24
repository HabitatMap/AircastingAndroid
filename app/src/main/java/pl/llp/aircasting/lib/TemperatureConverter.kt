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

        fun getAppropriateTemperatureValue(temperature: Double): Double {
            return if (singleton?.mSettings?.isCelsiusScaleEnabled() == true) temperatureFromFahrenheitToCelsius(
                temperature
            ) else temperature
        }

        fun getAppropriateTemperatureValue(temperature: Float): Float {
            return if (singleton?.mSettings?.isCelsiusScaleEnabled() == true) temperatureFromFahrenheitToCelsius(
                temperature
            ) else temperature
        }

        fun setAppropriateDetailedType(stream: MeasurementStream) {
            if (singleton?.mSettings?.isCelsiusScaleEnabled() == true) stream.detailedType = "C"
        }
    }

    private var mSettings: Settings? = settings
}

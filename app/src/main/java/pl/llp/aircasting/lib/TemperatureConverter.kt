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

        fun setAppropriateDetailedType(stream: MeasurementStream) {
            if (singleton?.mSettings?.isCelsiusScaleEnabled() == true) stream.detailedType = "C"
        }

        fun convertText(values: Float): String {
            val myValues: Float?

            return if (singleton?.mSettings?.isCelsiusScaleEnabled() == true) {
                myValues = temperatureFromFahrenheitToCelsius(values)
                "%d".format(myValues.toInt())

            } else "%d".format(values.toInt())
        }
    }

    private var mSettings: Settings? = settings
}

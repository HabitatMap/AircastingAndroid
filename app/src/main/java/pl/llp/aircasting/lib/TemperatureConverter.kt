package pl.llp.aircasting.lib

import pl.llp.aircasting.models.MeasurementStream

class TemperatureConverter {
    companion object {

        private var singleton: TemperatureConverter? = null

        fun setup(settings: Settings) {
            if (singleton == null) singleton = TemperatureConverter(settings)
        }

        fun get(): TemperatureConverter? {
            return singleton
        }

    }

    private var mSettings: Settings? = null

    private constructor(settings: Settings) {
        this.mSettings = settings
    }

    fun convertStream(stream: MeasurementStream): MeasurementStream {
        var measurementStream: MeasurementStream = stream

        if (stream.detailedType == "F" && mSettings?.isCelsiusScaleEnabled() == true) {
            measurementStream.detailedType = "C"
            stream.measurements.forEach { measurement ->
                measurement.value = temperaturefromFehreinheitToCelcius(measurement.value)
            }
        }

        if (stream.detailedType == "C" && mSettings?.isCelsiusScaleEnabled() == false) {
            measurementStream.detailedType = "F"
            stream.measurements.forEach { measurement ->
                measurement.value = temperatureFromCelsiusToFehrenheit(measurement.value)
            }
        }

        return measurementStream
    }

    private fun temperaturefromFehreinheitToCelcius(fahrenheitTemperature: Double): Double {
        return ((fahrenheitTemperature - 32) * 5) / 9
    }

    private fun temperatureFromCelsiusToFehrenheit(celsiusTemperature: Double): Double {
        return ((celsiusTemperature / 5) * 9) + 32
    }
}

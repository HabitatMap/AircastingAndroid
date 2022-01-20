package pl.llp.aircasting.lib

import pl.llp.aircasting.models.Measurement
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
            measurementStream = MeasurementStream(
                measurementStream.sensorPackageName,
                measurementStream.sensorName,
                measurementStream.measurementType,
                measurementStream.measurementShortType,
                measurementStream.unitName,
                measurementStream.unitSymbol,
                measurementStream.thresholdVeryLow,
                measurementStream.thresholdLow,
                measurementStream.thresholdMedium,
                measurementStream.thresholdHigh,
                measurementStream.thresholdVeryHigh,
                measurementStream.deleted,
                stream.measurements.map { measurement ->
                    Measurement(
                        temperaturefromFehreinheitToCelcius(measurement.value),
                        measurement.time,
                        measurement.latitude,
                        measurement.longitude,
                        measurement.averagingFrequency
                    )

                }
            )
            measurementStream.detailedType = "C"
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

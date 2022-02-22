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

        fun convertIfNecessary(temperature: Double): Double {
            return if (singleton!!.mSettings!!.isCelsiusScaleEnabled())
                temperatureFromFahrenheitToCelsius(temperature)
            else temperature
        }
    }

    private var mSettings: Settings? = settings

    fun convertStream(stream: MeasurementStream): MeasurementStream {
        var measurementStream: MeasurementStream = stream

//        if (stream.detailedType == "F" && mSettings?.isCelsiusScaleEnabled() == true) {
//            measurementStream = MeasurementStream(
//                measurementStream.sensorPackageName,
//                measurementStream.sensorName,
//                measurementStream.measurementType,
//                measurementStream.measurementShortType,
//                measurementStream.unitName,
//                measurementStream.unitSymbol,
//                measurementStream.thresholdVeryLow,
//                measurementStream.thresholdLow,
//                measurementStream.thresholdMedium,
//                measurementStream.thresholdHigh,
//                measurementStream.thresholdVeryHigh,
//                measurementStream.deleted,
//                stream.measurements.map { measurement ->
//                    Measurement(
//                        temperatureFromFahrenheitToCelsius(measurement.value),
//                        measurement.time,
//                        measurement.latitude,
//                        measurement.longitude,
//                        measurement.averagingFrequency
//                    )
//
//                }
//            )
//            measurementStream.detailedType = "C"
//        }
        // set back to measurementStream
        return stream
    }

}

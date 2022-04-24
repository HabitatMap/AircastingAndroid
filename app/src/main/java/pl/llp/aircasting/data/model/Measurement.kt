package pl.llp.aircasting.data.model

import pl.llp.aircasting.data.local.data_classes.ActiveSessionMeasurementDBObject
import pl.llp.aircasting.data.local.data_classes.MeasurementDBObject
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.data.api.responses.MeasurementResponse
import java.util.*

class Measurement(
    var value: Double = 0.0,
    val time: Date = Date(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    var averagingFrequency: Int = 1
    ) {

    constructor(event: NewMeasurementEvent, latitude: Double?, longitude: Double?):
            this(event.measuredValue, Date(event.creationTime), latitude, longitude)

    constructor(measurementDBObject: MeasurementDBObject): this(
        measurementDBObject.value,
        measurementDBObject.time,
        measurementDBObject.latitude,
        measurementDBObject.longitude,
        measurementDBObject.averaging_frequency
    )

    constructor(activeSessionMeasurementDBObject: ActiveSessionMeasurementDBObject): this(
        activeSessionMeasurementDBObject.value,
        activeSessionMeasurementDBObject.time,
        activeSessionMeasurementDBObject.latitude,
        activeSessionMeasurementDBObject.longitude
    )

    constructor(measurementResponse: MeasurementResponse): this(
        measurementResponse.value,
        DateConverter.fromString(measurementResponse.time) ?: Date(),
        measurementResponse.latitude,
        measurementResponse.longitude
    )

    constructor(measurementResponse: MeasurementResponse, averagingFrequency: Int): this(
        measurementResponse.value,
        DateConverter.fromString(measurementResponse.time) ?: Date(),
        measurementResponse.latitude,
        measurementResponse.longitude,
        averagingFrequency
    )

    enum class Level(val value: Int) {
        EXTREMELY_LOW(-1),
        LOW(0),
        MEDIUM(1),
        HIGH(2),
        VERY_HIGH(3),
        EXTREMELY_HIGH(4)
    }
/*
    T1..T5 are integer thresholds which guide how values should be displayed:
    - lower than T1 - extremely low - level = -1
    - between T1 and T2 - low / green - level = 0
    - between T2 and T3 - medium / yellow - level = 1
    - between T3 and T4 - high / orange - level = 2
    - between T4 and T5 - very high / red - level = 3
    - higher than T5 - extremely high - level = 4
 */

    fun getLevel(sensorThreshold: SensorThreshold): Level {
        return getLevel(value, sensorThreshold)
    }

    companion object {
        fun getLevel(value: Double, sensorThreshold: SensorThreshold): Level {
            if (value < sensorThreshold.thresholdVeryLow) return Level.EXTREMELY_LOW
            if (value >= sensorThreshold.thresholdVeryHigh) return Level.EXTREMELY_HIGH

            val index = sensorThreshold.levels.indexOfLast { level -> value >= level }
            return Level.values().first { it.value == index }
        }

        fun formatValue(value: Double?): String {
            if (value == null) {
                return "-"
            } else {
                return "%.0f".format(value)
            }
        }
    }
}

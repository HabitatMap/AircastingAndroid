package io.lunarlogic.aircasting.models

import io.lunarlogic.aircasting.database.data_classes.MeasurementDBObject
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.networking.responses.MeasurementResponse
import java.util.*

class Measurement(
    val value: Double,
    val time: Date,
    val latitude: Double? = null,
    val longitude: Double? = null) {

    constructor(event: NewMeasurementEvent, latitude: Double?, longitude: Double?):
            this(event.measuredValue, Date(event.creationTime), latitude, longitude)

    constructor(measurementDBObject: MeasurementDBObject): this(
        measurementDBObject.value,
        measurementDBObject.time,
        measurementDBObject.latitude,
        measurementDBObject.longitude
    )

    constructor(measurementResponse: MeasurementResponse): this(
        measurementResponse.value,
        DateConverter.fromString(measurementResponse.time) ?: Date(),
        measurementResponse.latitude,
        measurementResponse.longitude
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

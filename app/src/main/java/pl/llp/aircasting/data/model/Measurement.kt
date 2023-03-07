package pl.llp.aircasting.data.model

import pl.llp.aircasting.data.api.response.MeasurementOfStreamResponse
import pl.llp.aircasting.data.api.response.MeasurementResponse
import pl.llp.aircasting.data.local.entity.ActiveSessionMeasurementDBObject
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.events.NewMeasurementEvent
import java.util.*
import kotlin.math.round

class Measurement(
    var value: Double = 0.0,
    val time: Date = Date(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    var averagingFrequency: Int = 1
) {
    constructor(
        event: NewMeasurementEvent,
        location: Session.Location?,
        creationTime: Date = Date()
    ) : this(round(event.measuredValue), creationTime, location?.latitude, location?.longitude)

    constructor(measurementDBObject: MeasurementDBObject) : this(
        measurementDBObject.value,
        measurementDBObject.time,
        measurementDBObject.latitude,
        measurementDBObject.longitude,
        measurementDBObject.averagingFrequency
    )

    constructor(activeSessionMeasurementDBObject: ActiveSessionMeasurementDBObject) : this(
        activeSessionMeasurementDBObject.value,
        activeSessionMeasurementDBObject.time,
        activeSessionMeasurementDBObject.latitude,
        activeSessionMeasurementDBObject.longitude
    )

    constructor(measurementResponse: MeasurementResponse) : this(
        measurementResponse.value,
        DateConverter.fromString(measurementResponse.time) ?: Date(),
        measurementResponse.latitude,
        measurementResponse.longitude
    )

    constructor(measurementResponse: MeasurementOfStreamResponse) : this(
        measurementResponse.value,
        Date(measurementResponse.time),
        measurementResponse.latitude,
        measurementResponse.longitude
    )

    constructor(
        measurementResponse: MeasurementResponse,
        averagingFrequency: Int,
        timeZone: TimeZone = TimeZone.getDefault()
    ) : this(
        measurementResponse.value,
        DateConverter.fromString(measurementResponse.time, timeZone) ?: Date(),
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Measurement

        if (value != other.value) return false
        if (time != other.time) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (averagingFrequency != other.averagingFrequency) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + averagingFrequency
        return result
    }

    override fun toString(): String {
        return "Measurement(value=$value, time=$time, latitude=$latitude, longitude=$longitude, averagingFrequency=$averagingFrequency)"
    }

    companion object {
        fun getLevel(value: Double, sensorThreshold: SensorThreshold): Level {
            if (value < sensorThreshold.thresholdVeryLow) return Level.EXTREMELY_LOW
            if (value >= sensorThreshold.thresholdVeryHigh) return Level.EXTREMELY_HIGH

            val index = sensorThreshold.levels.indexOfLast { level -> value >= level }
            return Level.values().first { it.value == index }
        }

        fun formatValue(value: Double?): String {
            return if (value == null) {
                "-"
            } else {
                "%.0f".format(value)
            }
        }
    }
}

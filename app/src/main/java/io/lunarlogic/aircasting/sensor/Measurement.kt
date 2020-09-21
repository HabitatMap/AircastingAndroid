package io.lunarlogic.aircasting.sensor

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
/*
    T1..T5 are integer thresholds which guide how values should be displayed:
    - lower than T1 - extremely low / won't be displayed - level = null
    - between T1 and T2 - low / green - level = 0
    - between T2 and T3 - medium / yellow - level = 1
    - between T3 and T4 - high / orange - level = 2
    - between T4 and T5 - very high / red - level = 3
    - higher than T5 - extremely high / won't be displayed - level = null
 */
    fun getLevel(stream: MeasurementStream): Int? {
        if (value < stream.thresholdVeryLow) return null
        if (value >= stream.thresholdVeryHigh) return null

        return stream.levels.indexOfLast { level -> value >= level }
    }

    fun valueString(): String {
        return "%.0f".format(value)
    }
}

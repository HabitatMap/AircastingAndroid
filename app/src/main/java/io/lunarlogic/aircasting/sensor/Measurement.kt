package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.database.data_classes.MeasurementDBObject
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import java.util.*

class Measurement(
    val value: Double,
    val time: Date,
    val latitude: Double?,
    val longitude: Double?) {

    constructor(event: NewMeasurementEvent, latitude: Double?, longitude: Double?):
            this(event.measuredValue, Date(event.creationTime), latitude, longitude)
    constructor(measurementDBObject: MeasurementDBObject):
            this(
                measurementDBObject.value,
                measurementDBObject.time,
                measurementDBObject.latitude,
                measurementDBObject.longitude
            )
}
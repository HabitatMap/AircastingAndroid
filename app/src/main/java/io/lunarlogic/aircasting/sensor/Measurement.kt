package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.database.data_classes.MeasurementDBObject
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import java.util.*

class Measurement(
    private var mValue: Double,
    private var mTime: Date) {

    constructor(event: NewMeasurementEvent) : this(event.measuredValue, Date(event.creationTime))
    constructor(measurementDBObject: MeasurementDBObject): this(measurementDBObject.value, measurementDBObject.time)

    val value get() = mValue
    val time get() = mTime
}
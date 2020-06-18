package io.lunarlogic.aircasting.networking.params

import io.lunarlogic.aircasting.sensor.Measurement
import java.util.*

val MILLISECONDS_IN_SECOND = 1000

class MeasurementParams {
    constructor(measurement: Measurement) {
        this.value = measurement.value
        this.latitude = measurement.latitude
        this.longitude = measurement.longitude
        this.time = measurement.time
        this.milliseconds = measurement.time.time.rem(MILLISECONDS_IN_SECOND).toInt()
    }

    val longitude: Double?
    val latitude: Double?
    val timezone_offset = 0 // TODO: handle
    val milliseconds: Int
    val time: Date
    val value: Double?
}
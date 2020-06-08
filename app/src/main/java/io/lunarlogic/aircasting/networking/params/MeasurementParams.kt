package io.lunarlogic.aircasting.networking.params

import io.lunarlogic.aircasting.sensor.Measurement
import java.util.*

class MeasurementParams {
    constructor(measurement: Measurement) {
        this.value = measurement.value
        this.latitude = measurement.latitude
        this.longitude = measurement.longitude
    }

    val longitude: Double?
    val latitude: Double?
    val timezone_offset = 0 // TODO: handle
    val milliseconds = 141 // handle
    val time = Date() // handle
    val value: Double?
}
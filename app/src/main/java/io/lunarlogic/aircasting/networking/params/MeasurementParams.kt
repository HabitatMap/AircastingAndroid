package io.lunarlogic.aircasting.networking.params

import io.lunarlogic.aircasting.sensor.Measurement
import java.util.*

class MeasurementParams {
    constructor(measurement: Measurement) {
        this.value = measurement.value
    }

    val longitude = 19.9263968 // TODO: handle
    val latitude = 50.058191 // TODO: handle
    val timezone_offset = 0 // TODO: handle
    val milliseconds = 141 // handle
    val time = Date() // handle
    val value: Double?
}
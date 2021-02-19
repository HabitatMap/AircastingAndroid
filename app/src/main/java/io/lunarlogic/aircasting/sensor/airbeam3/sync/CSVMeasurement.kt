package io.lunarlogic.aircasting.sensor.airbeam3.sync

import io.lunarlogic.aircasting.models.Measurement
import java.util.*

class CSVMeasurement(val value: Double, val latitude: Double?, val longitude: Double?, val time: Date) {
    fun toMeasurement(): Measurement {
        return Measurement(value, time, latitude, longitude)
    }
}

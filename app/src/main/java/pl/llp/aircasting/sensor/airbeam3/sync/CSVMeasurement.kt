package pl.llp.aircasting.sensor.airbeam3.sync

import pl.llp.aircasting.models.Measurement
import java.util.*

class CSVMeasurement(val value: Double, val latitude: Double?, val longitude: Double?, val time: Date) {
    fun toMeasurement(): Measurement {
        return Measurement(value, time, latitude, longitude)
    }
}

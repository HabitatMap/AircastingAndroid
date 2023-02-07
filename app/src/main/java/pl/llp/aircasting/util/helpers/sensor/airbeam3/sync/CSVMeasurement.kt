package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.model.Measurement
import java.util.*

class CSVMeasurement(val value: Double, val latitude: Double?, val longitude: Double?, val time: Date) {
    fun toMeasurement(): Measurement {
        return Measurement(value, time, latitude, longitude)
    }

    override fun toString(): String {
        return "CSVMeasurement(value=$value, latitude=$latitude, longitude=$longitude, time=$time)"
    }
}

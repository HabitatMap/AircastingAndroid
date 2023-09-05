package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv

import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.util.helpers.services.AverageableMeasurement
import java.util.*

class CSVMeasurement(
    override var value: Double,
    override var latitude: Double?,
    override var longitude: Double?,
    override var time: Date,
    val averagingFrequency: Int
) : AverageableMeasurement {
    fun toMeasurement(): Measurement {
        return Measurement(value, time, latitude, longitude, averagingFrequency)
    }

    override fun toString(): String {
        return "CSVMeasurement(value=$value, latitude=$latitude, longitude=$longitude, time=$time)"
    }
}

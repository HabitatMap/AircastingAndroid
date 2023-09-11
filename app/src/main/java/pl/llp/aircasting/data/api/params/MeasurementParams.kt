package pl.llp.aircasting.data.api.params

import pl.llp.aircasting.data.api.util.Constants
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.CSVMeasurement
import java.util.*

class MeasurementParams {
    constructor(measurement: Measurement) {
        this.value = measurement.value
        this.latitude = measurement.latitude
        this.longitude = measurement.longitude
        this.time = measurement.time
        this.milliseconds = measurement.time.time.rem(Constants.MILLIS_IN_SECOND).toInt()
    }

    constructor(csvMeasurement: CSVMeasurement) {
        this.value = csvMeasurement.value
        this.latitude = csvMeasurement.latitude
        this.longitude = csvMeasurement.longitude
        this.time = csvMeasurement.time
        this.milliseconds = csvMeasurement.time.time.rem(Constants.MILLIS_IN_SECOND).toInt()
    }

    val longitude: Double?
    val latitude: Double?
    val milliseconds: Int
    val time: Date
    val value: Double?
}

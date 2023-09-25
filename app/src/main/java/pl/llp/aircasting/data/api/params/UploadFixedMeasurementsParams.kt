package pl.llp.aircasting.data.api.params

import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.CSVMeasurement
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.measurementStream.CSVMeasurementStream


class UploadFixedMeasurementsParams(
    sessionUUID: String,
    deviceId: String,
    stream: CSVMeasurementStream,
    csvMeasurements: List<CSVMeasurement>
) {

    val session_uuid: String = sessionUUID
    val sensor_package_name: String
    val sensor_name: String?
    val measurement_type: String?
    val measurement_short_type: String?
    val unit_name: String?
    val unit_symbol: String?
    val threshold_very_high: Int?
    val threshold_high: Int?
    val threshold_medium: Int?
    val threshold_low: Int?
    val threshold_very_low: Int?
    val measurements: List<MeasurementParams>

    init {
        this.sensor_package_name = stream.sensorPackageName(deviceId)
        this.sensor_name = stream.sensorName
        this.measurement_type = stream.measurementType
        this.measurement_short_type = stream.measurementShortType
        this.unit_name = stream.unitName
        this.unit_symbol = stream.unitSymbol
        this.threshold_very_high = stream.thresholdVeryHigh
        this.threshold_high = stream.thresholdHigh
        this.threshold_medium = stream.thresholdMedium
        this.threshold_low = stream.thresholdLow
        this.threshold_very_low = stream.thresholdVeryLow
        this.measurements = csvMeasurements.map {
            MeasurementParams(
                it
            )
        }
    }

    override fun toString(): String {
        return "UploadFixedMeasurementsParams(\n" +
                "$session_uuid,\n" +
                "$sensor_package_name,\n" +
                "$sensor_name,\n" +
                "$measurement_type,\n" +
                "$measurement_short_type,\n" +
                "$unit_name,\n" +
                "$unit_symbol,\n" +
                "$threshold_very_high,\n" +
                "$threshold_high,\n" +
                "$threshold_medium,\n" +
                "$threshold_low,\n" +
                "$threshold_very_low,\n" +
                "$measurements,\n" +
                ")"

    }
}

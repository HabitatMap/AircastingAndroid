package pl.llp.aircasting.data.api.params

import pl.llp.aircasting.data.model.MeasurementStream

class MeasurementStreamParams {
    constructor(stream: MeasurementStream) {
        this.sensor_package_name = stream.sensorPackageName
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
        this.measurements = stream.measurements.map {
            MeasurementParams(
                it
            )
        }
        this.deleted = stream.deleted
    }

    val deleted: Boolean
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
}

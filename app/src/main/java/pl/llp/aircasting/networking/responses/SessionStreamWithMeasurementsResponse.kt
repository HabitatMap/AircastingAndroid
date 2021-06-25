package pl.llp.aircasting.networking.responses

class SessionStreamWithMeasurementsResponse(
    sensor_name: String,
    sensor_package_name: String,
    unit_name: String,
    measurement_type: String,
    measurement_short_type: String,
    unit_symbol: String,
    threshold_very_low: Int,
    threshold_low: Int,
    threshold_medium: Int,
    threshold_high: Int,
    threshold_very_high: Int,
    deleted: Boolean,
    val measurements: List<MeasurementResponse>
): SessionStreamResponse(
    sensor_name,
    sensor_package_name,
    unit_name,
    measurement_type,
    measurement_short_type,
    unit_symbol,
    threshold_very_low,
    threshold_low,
    threshold_medium,
    threshold_high,
    threshold_very_high,
    deleted
)

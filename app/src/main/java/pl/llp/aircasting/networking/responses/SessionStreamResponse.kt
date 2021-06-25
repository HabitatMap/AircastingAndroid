package pl.llp.aircasting.networking.responses

open class SessionStreamResponse(
    val sensor_name: String,
    val sensor_package_name: String,
    val unit_name: String,
    val measurement_type: String,
    val measurement_short_type: String,
    val unit_symbol: String,
    val threshold_very_low: Int,
    val threshold_low: Int,
    val threshold_medium: Int,
    val threshold_high: Int,
    val threshold_very_high: Int,
    val deleted: Boolean
)

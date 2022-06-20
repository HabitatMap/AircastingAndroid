package pl.llp.aircasting.data.api.response

data class MeasurementOfStreamResponse(
    val value: Double,
    val time: Long,
    val latitude: Double,
    val longitude: Double
)

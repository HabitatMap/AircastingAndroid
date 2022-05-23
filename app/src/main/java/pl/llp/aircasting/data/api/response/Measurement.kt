package pl.llp.aircasting.data.api.response

data class Measurement(
    val latitude: Double,
    val longitude: Double,
    val time: Long,
    val value: Double
)
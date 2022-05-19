package pl.llp.aircasting.data.api.responses

data class Measurement(
    val latitude: Double,
    val longitude: Double,
    val time: Long,
    val value: Double
)
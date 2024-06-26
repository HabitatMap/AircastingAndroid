package pl.llp.aircasting.data.api.response

data class StreamOfGivenSessionResponse(
    val endTime: Long,
    val id: Int,
    val isIndoor: Boolean,
    val lastMeasurementValue: Double,
    val latitude: Double,
    val longitude: Double,
    val maxLatitude: Double,
    val maxLongitude: Double,
    val measurements: List<MeasurementOfStreamResponse>,
    val minLatitude: Double,
    val minLongitude: Double,
    val notes: List<Any>,
    val sensorName: String,
    val sensorUnit: String,
    val startTime: Long,
    val streamId: Int,
    val title: String,
    val username: String
)
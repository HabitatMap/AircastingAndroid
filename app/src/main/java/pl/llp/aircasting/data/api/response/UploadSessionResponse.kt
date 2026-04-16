package pl.llp.aircasting.data.api.response

class UploadSessionResponse(
    val location: String?,
    val session_token: String?,
    val streams: List<StreamInfo>?,
) {
    data class StreamInfo(val sensor_name: String, val sensor_type_id: Int)
}

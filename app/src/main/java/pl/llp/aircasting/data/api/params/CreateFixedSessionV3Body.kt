package pl.llp.aircasting.data.api.params

data class CreateFixedSessionV3Body(
    val uuid: String,
    val title: String,
    val latitude: Double?,
    val longitude: Double?,
    val contribute: Boolean,
    val is_indoor: Boolean,
    val airbeam: AirbeamInfo,
    val streams: List<StreamInfo>,
) {
    data class AirbeamInfo(val mac_address: String?, val model: String, val name: String)
    data class StreamInfo(val sensor_name: String, val unit_symbol: String)
}

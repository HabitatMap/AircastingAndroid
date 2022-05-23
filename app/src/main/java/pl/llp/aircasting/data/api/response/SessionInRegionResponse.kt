package pl.llp.aircasting.data.api.response

class SessionInRegionResponse(
    val type: String,
    val uuid: String,
    val title: String,
    val start_time_local: String,
    val end_time_local: String,
    val latitude: Double,
    val longitude: Double,
    val streams: HashMap<String, SessionStreamResponse>,
    val is_indoor: Boolean,
)
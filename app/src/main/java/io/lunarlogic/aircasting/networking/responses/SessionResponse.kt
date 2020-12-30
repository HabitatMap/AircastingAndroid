package io.lunarlogic.aircasting.networking.responses

class SessionResponse(
    val type: String,
    val uuid: String,
    val title: String,
    val tag_list: String,
    val start_time: String,
    val end_time: String,
    val latitude: Double,
    val longitude: Double,
    val deleted: Boolean,
    val version: Int,
    val streams: HashMap<String, SessionStreamResponse>,
    val location: String

    // TODO: add contribute field after adding this functionallity
    // TODO: add notes field after adding this functionallity
    // TODO: adding latitude, longitude and is_indoor might be needed when adding resume streaming functionallity
)

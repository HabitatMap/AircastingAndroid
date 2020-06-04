package io.lunarlogic.aircasting.networking.responses

class SessionResponse(
    val uuid: String,
    val title: String,
    val tag_list: String,
    val start_time: String,
    val end_time: String,
    val deleted: Boolean,
    val version: Int
)
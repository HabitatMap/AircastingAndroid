package io.lunarlogic.aircasting.networking.responses

import java.util.*

class SessionResponse(
    val uuid: String,
    val title: String,
    val tag_list: String,
    val start_time: Date,
    val end_time: Date,
    val deleted: Boolean,
    val version: Int
)
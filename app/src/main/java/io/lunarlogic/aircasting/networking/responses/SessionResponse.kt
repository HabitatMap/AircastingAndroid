package io.lunarlogic.aircasting.networking.responses

import io.lunarlogic.aircasting.models.Note

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
    val contribute: Boolean,
    val version: Int,
    val streams: HashMap<String, SessionStreamResponse>,
    val location: String,
    val is_indoor: Boolean,
    val notes: ArrayList<NoteResponse>
)

package io.lunarlogic.aircasting.networking.responses

import java.util.*

class NoteResponse (
    val sessionId: Long,
    val date: Long,
    val text: String,
    val latitude: Double?,
    val longitude: Double?,
    val number: Int,
    val photoPath: String?
)

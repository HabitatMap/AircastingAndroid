package io.lunarlogic.aircasting.networking.responses

import java.util.*

class NoteResponse (
    val sessionId: Long,
    val date: Date,
    val text: String,
    val latitude: Double?,
    val longitude: Double?

    // todo: fill it with fields
)

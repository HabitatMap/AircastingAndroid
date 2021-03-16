package io.lunarlogic.aircasting.networking.params

import io.lunarlogic.aircasting.models.Note

class NoteParams {
    constructor(note: Note) {
        this.date = note.date
        this.text = note.text
        this.latitude = note.latitude
        this.longitude = note.longitude
    }

    val date: Long
    val text: String
    val latitude: Double?
    val longitude: Double?
}

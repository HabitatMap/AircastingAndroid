package pl.llp.aircasting.data.model

import pl.llp.aircasting.data.local.entity.NoteDBObject
import java.util.*

class Note(
    val date: Date,
    var text: String,
    val latitude: Double?,
    val longitude: Double?,
    val number: Int,
    val photo_location: String?
) {

    constructor(noteDBObject: NoteDBObject) : this(
        noteDBObject.date,
        noteDBObject.text,
        noteDBObject.latitude,
        noteDBObject.longitude,
        noteDBObject.number,
        noteDBObject.photo_location
    )
}



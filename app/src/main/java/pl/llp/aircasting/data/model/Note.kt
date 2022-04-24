package pl.llp.aircasting.data.model

import pl.llp.aircasting.data.local.data_classes.NoteDBObject
import java.util.*

class Note(
    val date: Date,
    var text: String,
    val latitude: Double?,
    val longitude: Double?,
    val number: Int) {  //,todo: val photoPath: String

    constructor(noteDBObject: NoteDBObject): this(
        noteDBObject.date,
        noteDBObject.text,
        noteDBObject.latitude,
        noteDBObject.longitude,
        noteDBObject.number
    )

}



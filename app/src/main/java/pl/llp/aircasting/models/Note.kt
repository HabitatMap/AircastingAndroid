package pl.llp.aircasting.models

import pl.llp.aircasting.database.data_classes.NoteDBObject
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.DateConverter
import pl.llp.aircasting.networking.responses.NoteResponse
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

    constructor(noteResponse: NoteResponse): this(
        DateConverter.fromString(noteResponse.date)!!,
        noteResponse.text,
        noteResponse.latitude,
        noteResponse.longitude,
        noteResponse.number
    )
}



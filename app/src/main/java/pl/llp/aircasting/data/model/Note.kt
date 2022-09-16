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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Note

        if (date != other.date) return false
        if (text != other.text) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (number != other.number) return false
        if (photo_location != other.photo_location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + number
        result = 31 * result + (photo_location?.hashCode() ?: 0)
        return result
    }
}



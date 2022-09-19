package pl.llp.aircasting.data.local.entity

import androidx.room.*
import pl.llp.aircasting.data.model.Note
import java.util.*

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = SessionDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("session_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("session_id")
    ]
)
data class NoteDBObject(
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?,
    @ColumnInfo(name = "number") val number: Int,
    @ColumnInfo(name = "photo_location") val photo_location: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(sessionId: Long, note: Note) :
            this(
                sessionId,
                note.date,
                note.text,
                note.latitude,
                note.longitude,
                note.number,
                note.photo_location)
}

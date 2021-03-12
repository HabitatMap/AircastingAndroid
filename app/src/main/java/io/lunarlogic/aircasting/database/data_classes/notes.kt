package io.lunarlogic.aircasting.database.data_classes

import androidx.room.*
import io.lunarlogic.aircasting.models.Note
import java.util.*

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = SessionDBObject::class, //todo: these parameters a bit random now, have to check if they are correct
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
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?
) {
    @PrimaryKey(autoGenerate = true)
    var note_id: Long = 0

    constructor(note: Note):
            this(
                0,  //todo: this requires a fix, i dont have noteId and sessionId in the Note model
                note.text,
                note.date,
                note.latitude,
                note.longtitude
            )
    }

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: NoteDBObject)  // todo: is this Long needed here?

    @Query("SELECT * FROM notes WHERE session_id=:sessionId")
    fun allForSession(sessionId: Long): NoteDBObject?

//    @Query()
//    fun getNoteFromMap(latitude: Double?, longitude: Double?): NoteDBObject?

//    @Query
//    fun getNoteFromGraph(): NoteDBObject?
}

package io.lunarlogic.aircasting.database.data_classes

import androidx.room.*
import io.lunarlogic.aircasting.models.Note
import java.util.*

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = SessionDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("session_id"),
            onDelete = ForeignKey.CASCADE //todo: for cascade the migration is not working <?>
        )
    ],
    indices = [
        Index("session_id")
    ]
)
data class NoteDBObject(
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(sessionId: Long, note: Note):
            this(
                sessionId,  //todo: this requires a fix, i dont have noteId and sessionId in the Note model
                note.date,
                note.text,
                note.latitude,
                note.longitude
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

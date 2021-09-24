package pl.llp.aircasting.database.data_classes

import androidx.room.*
import pl.llp.aircasting.models.Note
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
    @ColumnInfo(name = "photo_location") val photoPath: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(sessionId: Long, note: Note):
            this(
                sessionId,
                note.date,
                note.text,
                note.latitude,
                note.longitude,
                note.number,
                note.photoPath
            )
    }

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: NoteDBObject): Long

    @Query("SELECT * FROM notes WHERE session_id=:sessionId")
    fun loadNotesBySessionId(sessionId: Long?): List<NoteDBObject?>

    @Query("UPDATE notes SET text=:text WHERE session_id=:sessionId AND number=:number")
    fun update(sessionId: Long, number: Int, text: String)

    @Query("UPDATE notes SET photo_location=:photoPath WHERE session_id=:sessionId AND number=:number")
    fun updatePhotoLocation(sessionId: Long, number: Int, photoPath: String?)

    @Query("DELETE FROM notes WHERE session_id=:sessionId AND number=:number")
    fun delete(sessionId: Long, number: Int)

    @Query("DELETE FROM notes WHERE session_id=:sessionId")
    fun deleteAllNotesForSessionWithId(sessionId: Long)

    @Query("SELECT * FROM notes WHERE session_id=:sessionId AND number=:number")
    fun loadNoteBySessionIdAndNumber(sessionId: Long, number: Int): NoteDBObject?

}

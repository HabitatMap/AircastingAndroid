package io.lunarlogic.aircasting.database.data_classes

import androidx.lifecycle.LiveData
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
    @ColumnInfo(name = "number") val number: Int
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
                note.number
            )
    }

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: NoteDBObject): Long

    @Query("UPDATE notes SET text=:text WHERE id=:id")
    fun update(id: Long, text: String)

    @Query("DELETE FROM notes WHERE id=:id")
    fun delete(id: Long)

    @Query("DELETE FROM notes WHERE session_id=:sessionId")
    fun deleteAllNotesForSessionWithId(sessionId: Long)

    @Query("SELECT * FROM notes WHERE session_id=:sessionId")
    fun loadNotesBySessionId(sessionId: Long): List<NoteDBObject?>

    @Query("SELECT * FROM notes WHERE session_id=:sessionId AND number=:number")
    fun loadNoteBySessionIdAndNumber(sessionId: Long, number: Int): NoteDBObject?

}

package pl.llp.aircasting.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.llp.aircasting.data.local.entity.NoteDBObject

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: NoteDBObject): Long

    @Query("SELECT * FROM notes WHERE session_id=:sessionId")
    fun loadNotesBySessionId(sessionId: Long): List<NoteDBObject?>

    @Query("UPDATE notes SET text=:text WHERE session_id=:sessionId AND number=:number")
    fun update(sessionId: Long, number: Int, text: String)

    @Query("DELETE FROM notes WHERE session_id=:sessionId AND number=:number")
    fun delete(sessionId: Long, number: Int)

    @Query("DELETE FROM notes WHERE session_id=:sessionId")
    fun deleteAllNotesForSessionWithId(sessionId: Long)

    @Query("SELECT * FROM notes WHERE session_id=:sessionId AND number=:number")
    fun loadNoteBySessionIdAndNumber(sessionId: Long, number: Int): NoteDBObject?
}
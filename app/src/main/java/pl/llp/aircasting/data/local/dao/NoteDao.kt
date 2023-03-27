package pl.llp.aircasting.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.llp.aircasting.data.local.entity.NoteDBObject

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteDBObject): Long

    @Query("SELECT * FROM notes WHERE session_id=:sessionId")
    suspend fun loadNotesBySessionId(sessionId: Long): List<NoteDBObject?>

    @Query("UPDATE notes SET text=:text WHERE session_id=:sessionId AND number=:number")
    suspend fun update(sessionId: Long, number: Int, text: String)

    @Query("DELETE FROM notes WHERE session_id=:sessionId AND number=:number")
    suspend fun delete(sessionId: Long, number: Int)
}
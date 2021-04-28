package io.lunarlogic.aircasting.database.repositories

import android.util.Log
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.NoteDBObject
import io.lunarlogic.aircasting.models.Note
import java.util.*

class NoteRepository {
    private val mDatabase = DatabaseProvider.get()

    fun insert(sessionId: Long, note: Note) {
        val noteDBObject =
            NoteDBObject(sessionId, note)

        mDatabase.notes().insert(noteDBObject)
    }

    fun update(sessionId: Long, note: Note) {
        mDatabase.notes().update(sessionId, note.number, note.text)
    }

    fun delete(sessionId: Long, note: Note) {
        mDatabase.notes().delete(sessionId, note.number)
    }

    fun deleteAllSessionsForSessionWithId(sessionId: Long) {
        mDatabase.notes().deleteAllNotesForSessionWithId(sessionId)
    }
}

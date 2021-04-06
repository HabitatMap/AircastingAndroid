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
        // I need to obtain old note object to get item id (primary key) in the database- i 'number' from new Note field because these 2 are the same with changed text in fact
        val oldNoteDBObject = mDatabase.notes().loadNoteBySessionIdAndNumber(sessionId, note.number)

        val noteDBObject = NoteDBObject(sessionId, note)

        oldNoteDBObject?.let{
            mDatabase.notes().update(oldNoteDBObject.id, noteDBObject.text)
        }
    }

    fun loadNoteForSessionWithId(sessionId: Long): List<NoteDBObject?> {
        return mDatabase.notes().loadNotesBySessionId(sessionId)
    }
}

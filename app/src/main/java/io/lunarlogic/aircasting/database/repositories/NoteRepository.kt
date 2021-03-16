package io.lunarlogic.aircasting.database.repositories

import android.util.Log
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.NoteDBObject
import io.lunarlogic.aircasting.models.Note
import java.util.*

class NoteRepository {
    private val mDatabase = DatabaseProvider.get()

    fun insert(sessionId: Long, note: Note) {
        Log.i("NOTE_REPO", "Inserting note to DB")
        val noteDBObject =
            NoteDBObject(sessionId, note)

        mDatabase.notes().insert(noteDBObject)
    }

    fun getIdOrInsert(sessionId: Long, note: Note): Long {
        var noteDBObject = mDatabase.notes().loadNoteBySessionId(sessionId)

        if (noteDBObject != null) return noteDBObject.id

        noteDBObject = NoteDBObject(
            sessionId,
            note
        )
        return mDatabase.notes().insert(noteDBObject)
    }

    fun updateNote(currentNote: Note, sessionUUID: UUID) {

    }
}

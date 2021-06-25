package pl.llp.aircasting.database.repositories

import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.data_classes.NoteDBObject
import pl.llp.aircasting.models.Note

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

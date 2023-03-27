package pl.llp.aircasting.data.local.repository

import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.NoteDBObject
import pl.llp.aircasting.data.model.Note
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor() {
    private val mDatabase = DatabaseProvider.get()

    suspend fun insert(sessionId: Long, note: Note) {
        val noteDBObject = NoteDBObject(sessionId, note)
        mDatabase.notes().insert(noteDBObject)
    }

    suspend fun update(sessionId: Long, note: Note) {
        mDatabase.notes().update(sessionId, note.number, note.text)
    }

    suspend fun delete(sessionId: Long, note: Note) {
        mDatabase.notes().delete(sessionId, note.number)
    }

    suspend fun getNotesForSessionWithId(sessionId: Long): List<NoteDBObject?> {
        return mDatabase.notes().loadNotesBySessionId(sessionId)
    }
}

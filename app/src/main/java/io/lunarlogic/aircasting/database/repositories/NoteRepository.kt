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

    fun loadNoteForSessionWithId(sessionId: Long): List<NoteDBObject?> {
        return mDatabase.notes().loadNotesBySessionId(sessionId)
    }

}

package io.lunarlogic.aircasting.database.repositories

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.NoteDBObject
import io.lunarlogic.aircasting.models.Note
import java.util.*

class NoteRepository {
    private val mDatabase = DatabaseProvider.get()

    fun insert(note: Note) { //todo: create noteDBObject class, and "notes" table
        val noteDBObject =
            NoteDBObject(note)
        return mDatabase.notes().insert(noteDBObject)
    }

    fun updateNote(currentNote: Note, sessionUUID: UUID) {

    }
}

package io.lunarlogic.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.models.Note
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter

class NoteObserver(
    private val mLifecycleOwner: LifecycleOwner,
    private val mSessionsViewModel: SessionsViewModel,
    private val mSessionPresenter: SessionPresenter
) {
    private val sessionsRepository = SessionsRepository()

    fun observe() {
        val sessionUUID = mSessionPresenter.sessionUUID
        sessionUUID ?: return
        var sessionId: Long? = 0
        DatabaseProvider.runQuery {
            sessionId = sessionsRepository.getSessionIdByUUID(sessionUUID)
        }

        var notesList: List<Note>

        mSessionsViewModel.loadNotesForSessionWithSessionId(sessionId!!).observe(mLifecycleOwner, Observer { notesListDBObject ->
            notesListDBObject?.let {
                notesList = notesListDBObject.map { noteDBObject -> Note(noteDBObject!!) } //todo: null check!
                mSessionPresenter.notes = notesList
            }
        })
    }
}

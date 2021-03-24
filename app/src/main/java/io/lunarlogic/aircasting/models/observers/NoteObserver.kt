package io.lunarlogic.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.models.Note
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import kotlinx.coroutines.CoroutineScope

class NoteObserver(
    private val mLifecycleOwner: LifecycleOwner,
    private val mSessionsViewModel: SessionsViewModel,
    private val mSessionPresenter: SessionPresenter,
    private val onSessionChangedCallback: (coroutineScope: CoroutineScope) -> Unit
) {

    fun observe() {
        val sessionUUID = mSessionPresenter.sessionUUID
        sessionUUID ?: return

        var session: Session
        var notesList: List<Note>

        mSessionsViewModel.loadLiveDataSessionForUploadBySessionUUID(sessionUUID).observe(mLifecycleOwner, Observer { sessionDBObject ->
            sessionDBObject?.let {
                session = Session(sessionDBObject)
                if (session.hasChangedFrom(mSessionPresenter.session)) {
                    DatabaseProvider.runQuery { coroutineScope ->
                        mSessionPresenter.session = session
                        notesList =
                            sessionDBObject.notes.map { noteDBObject ->
                                Note(noteDBObject)
                            } //todo: this map a bit random for now
                        mSessionPresenter.notes = notesList

                        onSessionChangedCallback.invoke(coroutineScope)
                    }
                }
            }
        })
    }
}

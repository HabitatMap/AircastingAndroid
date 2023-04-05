package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndNotesDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel


class MobileSessionObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    private val mSessionPresenter: SessionPresenter,
    private val onSessionChangedCallback: () -> Unit
) : SessionObserver<SessionWithStreamsAndNotesDBObject>(
    mLifecycleOwner,
    mSessionsViewModel,
    mSessionPresenter,
    onSessionChangedCallback
) {

    override fun buildSession(dbSession: SessionWithStreamsAndNotesDBObject): Session {
        return Session(dbSession)
    }

    override fun sessionLiveData(): LiveData<SessionWithStreamsAndNotesDBObject?>? {
        val sessionUUID = mSessionPresenter.sessionUUID
        sessionUUID ?: return null

        return mSessionsViewModel.loadSessionWithNotesAndStreamsByUUID(sessionUUID)
    }
}


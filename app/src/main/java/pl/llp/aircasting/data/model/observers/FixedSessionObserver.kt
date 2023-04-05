package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import pl.llp.aircasting.data.local.entity.CompleteSessionDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel

class FixedSessionObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    private val mSessionPresenter: SessionPresenter,
    private val onSessionChangedCallback: () -> Unit
): SessionObserver<CompleteSessionDBObject>(mLifecycleOwner, mSessionsViewModel, mSessionPresenter, onSessionChangedCallback) {

    override fun buildSession(dbSession: CompleteSessionDBObject): Session {
        return Session(dbSession)
    }

    override fun sessionLiveData(): LiveData<CompleteSessionDBObject?>? {
        val sessionUUID = mSessionPresenter.sessionUUID
        sessionUUID ?: return null

        return mSessionsViewModel.loadLiveDataCompleteSessionBySessionUUID(sessionUUID)
    }
}


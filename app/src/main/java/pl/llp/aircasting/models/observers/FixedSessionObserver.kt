package pl.llp.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import pl.llp.aircasting.database.data_classes.CompleteSessionDBObject
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.screens.dashboard.SessionPresenter
import kotlinx.coroutines.CoroutineScope

class FixedSessionObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    private val mSessionPresenter: SessionPresenter,
    private val onSessionChangedCallback: (coroutineScope: CoroutineScope) -> Unit
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


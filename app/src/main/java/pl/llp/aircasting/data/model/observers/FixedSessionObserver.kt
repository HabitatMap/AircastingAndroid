package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import pl.llp.aircasting.data.local.data_classes.CompleteSessionDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
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

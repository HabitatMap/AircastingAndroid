package io.lunarlogic.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.lunarlogic.aircasting.database.data_classes.CompleteSessionDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsAndNotesDBObject
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
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


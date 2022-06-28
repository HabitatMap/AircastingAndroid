package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.data.local.entity.SessionWithStreamsDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc

class FixedSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc?
): SessionsObserver<SessionWithStreamsDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {

    override fun buildSession(dbSession: SessionWithStreamsDBObject): Session {
        return Session(dbSession)
    }
}

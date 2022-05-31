package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.data.local.entity.ExtSessionsDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel

class ExternalSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc?
) : SessionsObserver<ExtSessionsDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {
    override fun buildSession(dbSession: ExtSessionsDBObject): Session {
        return Session(dbSession)
    }
}
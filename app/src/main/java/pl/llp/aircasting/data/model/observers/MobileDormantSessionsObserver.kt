package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.data.local.data_classes.SessionWithStreamsAndNotesDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc

class MobileDormantSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc?
): SessionsObserver<SessionWithStreamsAndNotesDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {

    override fun buildSession(dbSession: SessionWithStreamsAndNotesDBObject): Session {
        return Session(dbSession)
    }
}

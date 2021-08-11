package pl.llp.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.database.data_classes.SessionWithStreamsAndNotesDBObject
import pl.llp.aircasting.database.data_classes.SessionWithStreamsDBObject
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.screens.dashboard.SessionsViewMvc

class FixedSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc?
): SessionsObserver<SessionWithStreamsDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {

    override fun buildSession(dbSession: SessionWithStreamsDBObject): Session {
        return Session(dbSession)
    }
}

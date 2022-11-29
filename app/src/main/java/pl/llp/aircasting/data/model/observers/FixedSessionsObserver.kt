package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel

class FixedSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc?
) : SessionsObserver<SessionWithStreamsAndMeasurementsDBObject>(
    mLifecycleOwner,
    mSessionsViewModel,
    mViewMvc
) {

    override fun buildSession(dbSession: SessionWithStreamsAndMeasurementsDBObject): Session {
        return Session(dbSession)
    }
}

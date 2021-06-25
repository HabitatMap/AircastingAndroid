package pl.llp.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.database.data_classes.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.screens.dashboard.SessionsViewMvc

class ActiveSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc?
): SessionsObserver<SessionWithStreamsAndMeasurementsDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {

    override fun buildSession(dbSession: SessionWithStreamsAndMeasurementsDBObject): Session {
        return Session(dbSession)
    }
}

package pl.llp.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.database.data_classes.SessionWithStreamsAndLastMeasurementsDBObject
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.screens.dashboard.SessionsViewMvc

class MobileActiveSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc?
): SessionsObserver<SessionWithStreamsAndLastMeasurementsDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {

    override fun buildSession(dbSession: SessionWithStreamsAndLastMeasurementsDBObject): Session {
        return Session(dbSession)
    }
}

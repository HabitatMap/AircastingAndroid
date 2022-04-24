package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.data.local.data_classes.SessionWithStreamsAndLastMeasurementsDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc

class ActiveSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc?
): SessionsObserver<SessionWithStreamsAndLastMeasurementsDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {

    override fun buildSession(dbSession: SessionWithStreamsAndLastMeasurementsDBObject): Session {
        return Session(dbSession)
    }
}

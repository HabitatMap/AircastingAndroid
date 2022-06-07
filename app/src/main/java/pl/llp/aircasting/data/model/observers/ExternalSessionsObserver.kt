package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.data.local.entity.ExternalSessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel

class ExternalSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc?
) : SessionsObserver<ExternalSessionWithStreamsAndMeasurementsDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {
    override fun buildSession(dbSession: ExternalSessionWithStreamsAndMeasurementsDBObject): Session {
        return Session(dbSession)
    }
}
package io.lunarlogic.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsAndLastMeasurementsDBObject
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc

class MobileActiveSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc?
): SessionsObserver<SessionWithStreamsAndLastMeasurementsDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {

    override fun buildSession(dbSession: SessionWithStreamsAndLastMeasurementsDBObject): Session {
        return Session(dbSession)
    }
}

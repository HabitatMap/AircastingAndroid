package io.lunarlogic.aircasting.models

import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc

class DormantSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc
): SessionsObserver<SessionWithStreamsDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {

    override fun buildSession(dbSession: SessionWithStreamsDBObject): Session {
        return Session(dbSession)
    }
}

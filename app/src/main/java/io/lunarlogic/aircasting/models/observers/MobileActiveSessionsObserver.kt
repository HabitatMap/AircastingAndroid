package io.lunarlogic.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.database.data_classes.CompleteSessionDBObject
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc

class MobileActiveSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc
): SessionsObserver<CompleteSessionDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {

    override fun buildSession(dbCompleteSession: CompleteSessionDBObject): Session {
        return Session(dbCompleteSession)
    }
}

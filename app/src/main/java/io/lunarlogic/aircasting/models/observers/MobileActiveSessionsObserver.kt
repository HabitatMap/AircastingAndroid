package io.lunarlogic.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.database.data_classes.SessionForUploadDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsAndMeasurementsDBObject
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc

class MobileActiveSessionsObserver(
    mLifecycleOwner: LifecycleOwner,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionsViewMvc
): SessionsObserver<SessionForUploadDBObject>(mLifecycleOwner, mSessionsViewModel, mViewMvc) {

    override fun buildSession(dbSession: SessionForUploadDBObject): Session {
        return Session(dbSession)
    }
}

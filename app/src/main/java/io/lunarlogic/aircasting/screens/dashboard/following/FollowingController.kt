package io.lunarlogic.aircasting.screens.dashboard.following

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.dashboard.SessionsController
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc
import io.lunarlogic.aircasting.models.Session

class FollowingController(
    mRootActivity: FragmentActivity?,
    private val mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    mSettings: Settings
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mLifecycleOwner, mSettings),
    SessionsViewMvc.Listener {

    init {
        mSessionsLiveData = loadSessions()
    }

    override fun loadSessions(): LiveData<PagedList<SessionWithStreamsDBObject>> {
        return mSessionsViewModel.loadFollowingSessionsWithMeasurements()
    }

    override fun onRecordNewSessionClicked() {
        startNewSession(Session.Type.FIXED)
    }

    override fun onStopSessionClicked(sessionUUID: String) {

    }

    override fun onDeleteSessionClicked(sessionUUID: String) {

    }
}

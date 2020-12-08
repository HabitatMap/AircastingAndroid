package io.lunarlogic.aircasting.screens.dashboard.active

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.lib.NavigationController
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.observers.ActiveSessionsObserver
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.DashboardPagerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsController
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc
import org.greenrobot.eventbus.EventBus

class MobileActiveController(
    mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    mSettings: Settings
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings),
    SessionsViewMvc.Listener {

    private var mSessionsObserver = ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadMobileActiveSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onRecordNewSessionClicked() {
        startNewSession(Session.Type.MOBILE)
    }

    override fun onStopSessionClicked(sessionUUID: String) {
        val event = StopRecordingEvent(sessionUUID)
        EventBus.getDefault().post(event)

        val tabId = DashboardPagerAdapter.tabIndexForSessionType(
            Session.Type.MOBILE,
            Session.Status.FINISHED
        )
        NavigationController.goToDashboard(tabId)
    }

    override fun onDeleteSessionClicked(sessionUUID: String) {
        // do nothing
    }

    override fun onExpandSessionCard(session: Session) {
        // do nothing
    }
}

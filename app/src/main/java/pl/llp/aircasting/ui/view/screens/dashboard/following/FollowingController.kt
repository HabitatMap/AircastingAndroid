package pl.llp.aircasting.ui.view.screens.dashboard.following

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.ActiveSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedController
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.adjustMenuVisibility
import pl.llp.aircasting.util.extensions.expandedCards

class FollowingController(
    private val mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc?,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    mContext: Context?
) : FixedController(
    mRootActivity,
    mViewMvc,
    mSessionsViewModel,
    mLifecycleOwner,
    mSettings,
    mApiServiceFactory,
    mRootActivity!!.supportFragmentManager,
    mContext
) {

    private var mLocalSessionsObserver =
        ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun onResume() {
        super.onResume()
        mRootActivity?.adjustMenuVisibility(true, mSettings.followedSessionsCount())
    }

    override fun registerSessionsObserver() {
        mLocalSessionsObserver.observe(mSessionsViewModel.loadFollowingSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mLocalSessionsObserver.stop()
    }

    override fun onExpandSessionCard(session: Session) {
        super.onExpandSessionCard(session)
        expandedCards()?.add(session.uuid)
    }

    override fun onCollapseSessionCard(session: Session) {
        expandedCards()?.remove(session.uuid)
    }
}

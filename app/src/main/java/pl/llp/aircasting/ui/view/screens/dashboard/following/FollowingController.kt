package pl.llp.aircasting.ui.view.screens.dashboard.following

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.ActiveSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsController
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.search.SearchFixedSessionActivity
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
    val mContext: Context?
) : SessionsController(
    mRootActivity,
    mViewMvc,
    mSessionsViewModel,
    mSettings,
    mApiServiceFactory,
    mRootActivity!!.supportFragmentManager,
    mContext
),
    SessionsViewMvc.Listener {

    private var mLocalSessionsObserver =
        ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun onResume() {
        super.onResume()
        mRootActivity?.adjustMenuVisibility(true, mSettings.getFollowedSessionsNumber())
    }

    override fun registerSessionsObserver() {
        mLocalSessionsObserver.observe(mSessionsViewModel.loadFollowingSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mLocalSessionsObserver.stop()
    }

    override fun onRecordNewSessionClicked() {
        mRootActivity?.let {
            Navigation.findNavController(it, R.id.nav_host_fragment)
                .navigate(R.id.navigation_lets_begin)
        }
    }

    override fun onExploreNewSessionsClicked() {
        val intent = Intent(mContext, SearchFixedSessionActivity::class.java)
        mContext?.startActivity(intent)
    }

    override fun onExpandSessionCard(session: Session) {
        super.onExpandSessionCard(session)
        expandedCards()?.add(session.uuid)
    }

    override fun onCollapseSessionCard(session: Session) {
        expandedCards()?.remove(session.uuid)
    }

    override fun onEditSessionClicked(session: Session) {
        // do nothing
    }

    override fun onShareSessionClicked(session: Session) {
        // do nothing
    }

    override fun onDeleteStreamsPressed(session: Session) {
        // do nothing
    }
}

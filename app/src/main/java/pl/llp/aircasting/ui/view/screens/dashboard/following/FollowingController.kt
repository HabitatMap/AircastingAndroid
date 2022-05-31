package pl.llp.aircasting.ui.view.screens.dashboard.following

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.data.model.observers.ActiveSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsController
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.search.SearchFixedSessionsActivity
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.adjustMenuVisibility

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

    private var mSessionsObserver =
        ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun onResume() {
        super.onResume()
        mRootActivity?.let { adjustMenuVisibility(it, true, mSettings.getFollowedSessionsNumber()) }
    }

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadFollowingSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onRecordNewSessionClicked() {
        mRootActivity?.let {
            Navigation.findNavController(it, R.id.nav_host_fragment)
                .navigate(R.id.navigation_lets_begin)
        }
    }

    override fun onExploreNewSessionsClicked() {
        val intent = Intent(mContext, SearchFixedSessionsActivity::class.java)
        mContext?.startActivity(intent)
    }

    override fun onEditSessionClicked(localSession: LocalSession) {
        // do nothing
    }

    override fun onShareSessionClicked(localSession: LocalSession) {
        // do nothing
    }

    override fun onDeleteStreamsPressed(localSession: LocalSession) {
        // do nothing
    }

    override fun onExpandSessionCard(localSession: LocalSession) {
        // do nothing
    }

    override fun onFinishSessionConfirmed(localSession: LocalSession) {
        // do nothing
    }

    override fun onFinishAndSyncSessionConfirmed(localSession: LocalSession) {
        // do nothing
    }
}

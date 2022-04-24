package pl.llp.aircasting.ui.view.screens.dashboard.following

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import pl.llp.aircasting.R
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.adjustMenuVisibility
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.data.model.observers.ActiveSessionsObserver
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsController
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc

class FollowingController(
    private val mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc?,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    mContext: Context?
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings, mApiServiceFactory, mRootActivity!!.supportFragmentManager, mContext),
    SessionsViewMvc.Listener {

    private var mSessionsObserver = ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

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

    override fun onEditSessionClicked(session: Session) {
        // do nothing
    }

    override fun onShareSessionClicked(session: Session) {
        // do nothing
    }

    override fun onDeleteStreamsPressed(session: Session) {
        // do nothing
    }

    override fun onExpandSessionCard(session: Session) {
        // do nothing
    }

    override fun onFinishSessionConfirmed(session: Session) {
        // do nothing
    }

    override fun onFinishAndSyncSessionConfirmed(session: Session) {
        // do nothing
    }
}

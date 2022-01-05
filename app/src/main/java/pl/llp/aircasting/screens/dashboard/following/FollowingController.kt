package pl.llp.aircasting.screens.dashboard.following

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.lib.AppBar
import pl.llp.aircasting.lib.NavigationController
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.models.observers.ActiveSessionsObserver
import pl.llp.aircasting.models.observers.MobileActiveSessionsObserver
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.screens.dashboard.SessionsController
import pl.llp.aircasting.screens.dashboard.SessionsViewMvc

class FollowingController(
    mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc?,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val mContext: Context?
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings, mApiServiceFactory, mRootActivity!!.supportFragmentManager, mContext),
    SessionsViewMvc.Listener {

    private var mSessionsObserver = ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun onResume() {
        super.onResume()

        val followingSessionsNumber = mSettings.getFollowedSessionsNumber()
        AppBar.adjustMenuVisibility(true, followingSessionsNumber)
    }

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadFollowingSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onRecordNewSessionClicked() {
        NavigationController.goToLetsStart()
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

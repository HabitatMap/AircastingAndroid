package io.lunarlogic.aircasting.screens.dashboard.following

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.observers.ActiveSessionsObserver
import io.lunarlogic.aircasting.screens.dashboard.SessionsController
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.ConnectivityManager

class FollowingController(
    mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val mContext: Context?
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings, mApiServiceFactory, mRootActivity!!.supportFragmentManager, mContext),
    SessionsViewMvc.Listener {

    private var mSessionsObserver = ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadFollowingSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onRecordNewSessionClicked() {
        if (!ConnectivityManager.isConnected(mContext)){
            Toast.makeText(mContext, "You need to have internet connection to create fixed session", Toast.LENGTH_LONG).show()
            return
        }

        startNewSession(Session.Type.FIXED)
    }

    override fun onStopSessionClicked(sessionUUID: String) {
        // do nothing
    }

    override fun onEditSessionClicked(session: Session) {
        // do nothing
    }

    override fun onShareSessionClicked(session: Session) {
        // do nothing
    }

    override fun onDeleteSessionClicked(sessionUUID: String) {
        // do nothing
    }

    override fun onExpandSessionCard(session: Session) {
        // do nothing
    }
}

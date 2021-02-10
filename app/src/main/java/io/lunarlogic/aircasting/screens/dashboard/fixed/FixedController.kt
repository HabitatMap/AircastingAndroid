package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.events.DeleteSessionEvent
import io.lunarlogic.aircasting.lib.NavigationController
import io.lunarlogic.aircasting.events.DeleteStreamsEvent
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.observers.DormantSessionsObserver
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.ConnectivityManager
import io.lunarlogic.aircasting.screens.dashboard.EditSessionBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.*
import org.greenrobot.eventbus.EventBus

class FixedController(
    private val mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    fragmentManager: FragmentManager,
    private val mContext: Context?
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings, mApiServiceFactory, fragmentManager, mContext),
    SessionsViewMvc.Listener, EditSessionBottomSheet.Listener {

    private var mSessionsObserver = DormantSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadFixedSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onRecordNewSessionClicked() {
        if (!ConnectivityManager.isConnected(mContext)) {
            Toast.makeText(mContext, mContext?.getString(R.string.fixed_session_no_internet_connection), Toast.LENGTH_LONG).show()
            return
        }

        NavigationController.goToLetsStart()
    }

    override fun onFinishSessionConfirmed(session: Session) {
        // do nothing
    }

    override fun onFinishAndSyncSessionConfirmed(session: Session) {
        // do nothing
    }

}

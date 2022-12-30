package pl.llp.aircasting.ui.view.screens.dashboard.fixed

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.observers.FixedSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsController
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.search.SearchFixedSessionActivity
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings

open class FixedController(
    private val mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc?,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    fragmentManager: FragmentManager,
    val mContext: Context?
) : SessionsController(
    mRootActivity,
    mViewMvc,
    mSessionsViewModel,
    mSettings,
    mApiServiceFactory,
    fragmentManager,
    mContext
),
    SessionsViewMvc.Listener {

    private var mSessionsObserver =
        FixedSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadFixedSessionsWithMeasurements())
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
        val intent = Intent(mContext, SearchFixedSessionActivity::class.java)
        mContext?.startActivity(intent)
    }
}

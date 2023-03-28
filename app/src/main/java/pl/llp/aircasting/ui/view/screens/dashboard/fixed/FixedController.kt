package pl.llp.aircasting.ui.view.screens.dashboard.fixed

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.DownloadMeasurementsService
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.model.observers.FixedSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsController
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.helpers.SessionFollower
import pl.llp.aircasting.ui.view.screens.search.SearchFixedSessionActivity
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.exceptions.ErrorHandler

@AssistedFactory
interface FixedControllerFactory {
    fun create(
        mRootActivity: FragmentActivity?,
        mViewMvc: SessionsViewMvc?,
        mLifecycleOwner: LifecycleOwner,
        fragmentManager: FragmentManager,
        mContext: Context?
    ): FixedController
}
open class FixedController @AssistedInject constructor(
    @Assisted private val mRootActivity: FragmentActivity?,
    @Assisted mViewMvc: SessionsViewMvc?,
    @Assisted private val mLifecycleOwner: LifecycleOwner,
    @Assisted fragmentManager: FragmentManager,
    @Assisted private val mContext: Context?,
    private val mSessionsViewModel: SessionsViewModel,
    mApiService: ApiService,
    mErrorHandler: ErrorHandler,
    mDownloadService: SessionDownloadService,
    mDownloadMeasurementsService: DownloadMeasurementsService,
    mActiveSessionsRepository: ActiveSessionMeasurementsRepository,
    sessionFollower: SessionFollower
) : SessionsController(
    mRootActivity,
    mViewMvc,
    fragmentManager,
    mContext,
    mSessionsViewModel,
    mApiService,
    mErrorHandler,
    mDownloadService,
    mDownloadMeasurementsService,
    mActiveSessionsRepository,
    sessionFollower
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

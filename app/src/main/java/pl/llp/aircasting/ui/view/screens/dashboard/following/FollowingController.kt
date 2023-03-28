package pl.llp.aircasting.ui.view.screens.dashboard.following

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.data.api.services.DownloadMeasurementsService
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.ActiveSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedController
import pl.llp.aircasting.ui.view.screens.dashboard.helpers.SessionFollower
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.adjustMenuVisibility
import pl.llp.aircasting.util.extensions.expandedCards

@AssistedFactory
interface FollowingControllerFactory {
    fun create(
        mRootActivity: FragmentActivity?,
        mViewMvc: SessionsViewMvc?,
        mLifecycleOwner: LifecycleOwner,
        fragmentManager: FragmentManager,
        mContext: Context?
    ): FollowingController
}

class FollowingController @AssistedInject constructor(
    @Assisted private val mRootActivity: FragmentActivity?,
    @Assisted private val mViewMvc: SessionsViewMvc?,
    @Assisted private val mLifecycleOwner: LifecycleOwner,
    @Assisted fragmentManager: FragmentManager,
    @Assisted mContext: Context?,
    private val mSessionsViewModel: SessionsViewModel,
    @Authenticated mApiService: ApiService,
    mErrorHandler: ErrorHandler,
    mDownloadService: SessionDownloadService,
    mDownloadMeasurementsService: DownloadMeasurementsService,
    mActiveSessionsRepository: ActiveSessionMeasurementsRepository,
    sessionFollower: SessionFollower,
    private val mSettings: Settings
) : FixedController(
    mRootActivity,
    mViewMvc,
    mLifecycleOwner,
    fragmentManager,
    mContext,
    mSessionsViewModel,
    mApiService,
    mErrorHandler,
    mDownloadService,
    mDownloadMeasurementsService,
    mActiveSessionsRepository,
    sessionFollower,
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

    override fun onFollowButtonClicked(session: Session) {
        super.onFollowButtonClicked(session)
        mRootActivity?.adjustMenuVisibility(true, mSettings.followedSessionsCount())
    }

    override fun onUnfollowButtonClicked(session: Session) {
        super.onUnfollowButtonClicked(session)
        mRootActivity?.adjustMenuVisibility(true, mSettings.followedSessionsCount())
    }
}

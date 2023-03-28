package pl.llp.aircasting.ui.view.screens.dashboard.dormant

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.data.api.services.DownloadMeasurementsService
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.MobileDormantSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsController
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.helpers.SessionFollower
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.exceptions.ErrorHandler

@AssistedFactory
interface MobileDormantControllerFactory {
    fun create(
        mRootActivity: FragmentActivity?,
        mViewMvc: SessionsViewMvc?,
        mLifecycleOwner: LifecycleOwner,
        fragmentManager: FragmentManager,
        mContext: Context?,
    ): MobileDormantController
}

class MobileDormantController @AssistedInject constructor(
    mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc?,
    mLifecycleOwner: LifecycleOwner,
    fragmentManager: FragmentManager,
    mContext: Context?,
    private val mSessionsViewModel: SessionsViewModel,
    @Authenticated mApiService: ApiService,
    mErrorHandler: ErrorHandler,
    mDownloadService: SessionDownloadService,
    mDownloadMeasurementsService: DownloadMeasurementsService,
    mActiveSessionsRepository: ActiveSessionMeasurementsRepository,
    sessionFollower: SessionFollower,
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
    sessionFollower,
), SessionsViewMvc.Listener {

    private var mSessionsObserver =
        MobileDormantSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadMobileDormantSessionsWithMeasurementsAndNotes())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onRecordNewSessionClicked() {
        startNewSession(Session.Type.MOBILE)
    }

    override fun onExploreNewSessionsClicked() {
        // do nothing
    }
}

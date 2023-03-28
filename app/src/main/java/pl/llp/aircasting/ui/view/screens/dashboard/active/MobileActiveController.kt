package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.data.api.services.DownloadMeasurementsService
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.ActiveSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsController
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.helpers.SessionFollower
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.expandedCards
import pl.llp.aircasting.util.extensions.safeRegister

@AssistedFactory
interface MobileActiveControllerFactory {
    fun create(
        mRootActivity: FragmentActivity?,
        mViewMvc: SessionsViewMvc?,
        mLifecycleOwner: LifecycleOwner,
        fragmentManager: FragmentManager,
        mContext: Context?
    ): MobileActiveController
}

class MobileActiveController @AssistedInject constructor(
    @Assisted mRootActivity: FragmentActivity?,
    @Assisted private val mViewMvc: SessionsViewMvc?,
    @Assisted mLifecycleOwner: LifecycleOwner,
    @Assisted fragmentManager: FragmentManager,
    @Assisted mContext: Context?,
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
    sessionFollower
), SessionsViewMvc.Listener {

    private var mSessionsObserver =
        ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun onCreate() {
        super.onCreate()
    }

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadMobileActiveSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().safeRegister(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onRecordNewSessionClicked() {
        startNewSession(Session.Type.MOBILE)
    }

    override fun onExploreNewSessionsClicked() {
        // Do nothing
    }

    override fun onExpandSessionCard(session: Session) {
        super.onExpandSessionCard(session)
        expandedCards()?.add(session.uuid)
    }

    override fun onCollapseSessionCard(session: Session) {
        expandedCards()?.remove(session.uuid)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        val deviceId = event.deviceId ?: return

        mViewMvc?.hideLoaderFor(deviceId)
    }
}

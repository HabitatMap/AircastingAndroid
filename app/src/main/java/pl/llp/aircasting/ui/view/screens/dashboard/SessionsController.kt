package pl.llp.aircasting.ui.view.screens.dashboard

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.DownloadMeasurementsService
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.helpers.SessionFollower
import pl.llp.aircasting.ui.view.screens.new_session.NewSessionActivity
import pl.llp.aircasting.ui.view.screens.session_view.graph.GraphActivity
import pl.llp.aircasting.ui.view.screens.session_view.map.MapActivity
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler

abstract class SessionsController(
    private var mRootActivity: FragmentActivity?,
    private var mViewMvc: SessionsViewMvc?,
    private val mSessionsViewModel: SessionsViewModel,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    val fragmentManager: FragmentManager,
    private var context: Context?,
    private val mApiService: ApiService = mApiServiceFactory.get(mSettings.getAuthToken()!!),
    protected val mErrorHandler: ErrorHandler = ErrorHandler(mRootActivity!!),
    val mDownloadService: SessionDownloadService = SessionDownloadService(
        mApiService,
        mErrorHandler
    ),
    mSessionRepository: SessionsRepository = SessionsRepository(),
    private val mDownloadMeasurementsService: DownloadMeasurementsService =
        DownloadMeasurementsService(mApiService, mErrorHandler),
    private val mActiveSessionsRepository: ActiveSessionMeasurementsRepository =
        ActiveSessionMeasurementsRepository(),
    private val sessionFollower: SessionFollower =
        SessionFollower(mSettings, mActiveSessionsRepository, mSessionRepository),
) : SessionsViewMvc.Listener {
    protected abstract fun registerSessionsObserver()
    protected abstract fun unregisterSessionsObserver()

    open fun onCreate() { /* Do nothing */
    }

    open fun onResume() {
        registerSessionsObserver()
        mViewMvc?.registerListener(this)
    }

    open fun onPause() {
        unregisterSessionsObserver()
        mViewMvc?.unregisterListener(this)
    }

    fun onDestroy() {
        unregisterSessionsObserver()
        mViewMvc = null
        context = null
    }

    protected fun startNewSession(sessionType: Session.Type) {
        NewSessionActivity.start(mRootActivity, sessionType)
    }

    override fun onFollowButtonClicked(session: Session) {
        sessionFollower.follow(session)
    }

    override fun onUnfollowButtonClicked(session: Session) {
        session.resetFollowedAtAndOrder()
        sessionFollower.unfollow(session)
    }

    override fun onMapButtonClicked(session: Session, sensorName: String?) {
        MapActivity.start(mRootActivity, sensorName, session.uuid, session.tab)
    }

    override fun onGraphButtonClicked(session: Session, sensorName: String?) {
        GraphActivity.start(mRootActivity, sensorName, session.uuid, session.tab)
    }

    private suspend fun reloadSession(session: Session) {
        val dbSessionWithMeasurements =
            mSessionsViewModel.reloadSessionWithMeasurementsSuspend(session.uuid)

        dbSessionWithMeasurements?.let {
            val reloadedSession = Session(it)
            mViewMvc?.hideLoaderFor(session)
            mViewMvc?.reloadSession(reloadedSession)
        }
    }

    override fun onExpandSessionCard(session: Session) {
        mViewMvc?.showLoaderFor(session)
        mSessionsViewModel.viewModelScope.launch {
            mDownloadMeasurementsService.downloadMeasurements(session)
            reloadSession(session)
        }
    }
}

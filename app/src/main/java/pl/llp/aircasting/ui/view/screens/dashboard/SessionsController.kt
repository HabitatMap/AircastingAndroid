package pl.llp.aircasting.ui.view.screens.dashboard

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.DownloadMeasurementsService
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.helpers.SessionFollower
import pl.llp.aircasting.ui.view.screens.new_session.NewSessionActivity
import pl.llp.aircasting.ui.view.screens.session_view.graph.GraphActivity
import pl.llp.aircasting.ui.view.screens.session_view.map.MapActivity
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.events.StreamSelectedEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.safeRegister


abstract class SessionsController(
    private var mRootActivity: FragmentActivity?,
    private var mViewMvc: SessionsViewMvc?,
    val fragmentManager: FragmentManager,
    private var context: Context?,
    private val mSessionsViewModel: SessionsViewModel,
    private val mApiService: ApiService,
    protected val mErrorHandler: ErrorHandler,
    val mDownloadService: SessionDownloadService,
    private val mDownloadMeasurementsService: DownloadMeasurementsService,
    private val mActiveSessionsRepository: ActiveSessionMeasurementsRepository,
    private val sessionFollower: SessionFollower,
) : SessionsViewMvc.Listener {
    protected abstract fun registerSessionsObserver()
    protected abstract fun unregisterSessionsObserver()

    open fun onCreate() { /* Do nothing */
    }

    open fun onResume() {
        registerSessionsObserver()
        EventBus.getDefault().safeRegister(this)
        mViewMvc?.registerListener(this)
    }

    open fun onPause() {
        unregisterSessionsObserver()
        EventBus.getDefault().unregister(this)
        mViewMvc?.unregisterListener(this)
    }

    fun onDestroy() {
        unregisterSessionsObserver()
        mViewMvc = null
        context = null
    }

    @Subscribe(sticky = true)
    fun onMessageEvent(detailsView: StreamSelectedEvent) {
        val session = detailsView.session ?: return

        mViewMvc?.reloadSession(session)
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
        // TODO: first()?
        mSessionsViewModel.reloadSessionWithMeasurements(session.uuid).collect { dbSession ->
            dbSession?.let {
                val reloadedSession = Session(it)
                mViewMvc?.hideLoaderFor(session)
                mViewMvc?.reloadSession(reloadedSession)
            }
        }
    }

    override fun onExpandSessionCard(session: Session) {
        mViewMvc?.showLoaderFor(session)
        mSessionsViewModel.viewModelScope.launch {
            mDownloadMeasurementsService.downloadMeasurements(session.uuid)
            reloadSession(session)
        }
    }
    suspend fun getReloadedSession(uuid: String): SessionWithStreamsAndMeasurementsDBObject? =
        mSessionsViewModel.reloadSessionWithMeasurements(uuid).firstOrNull()
}

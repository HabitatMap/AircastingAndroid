package io.lunarlogic.aircasting.screens.dashboard

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.NavigationController
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.DownloadMeasurementsService
import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.screens.map.MapActivity
import io.lunarlogic.aircasting.sensor.SensorThreshold
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.coroutines.CoroutineScope


abstract class SessionsController(
    private val mRootActivity: FragmentActivity?,
    private val mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    private val mLifecycleOwner: LifecycleOwner,
    private val mSettings: Settings
) : SessionsViewMvc.Listener {
    private val mErrorHandler = ErrorHandler(mRootActivity!!)
    private val mApiService =  ApiServiceFactory.get(mSettings.getAuthToken()!!)
    protected val mMobileSessionsSyncService = SessionsSyncService.get(mApiService, mErrorHandler)
    private val mDownloadMeasurementsService = DownloadMeasurementsService(mApiService, mErrorHandler)

    protected lateinit var mSessionsLiveData: LiveData<List<SessionWithStreamsDBObject>>
    private var mSessions = hashMapOf<String, Session>()
    private var mSensorThresholds = hashMapOf<String, SensorThreshold>()

    private var mSessionsObserver = Observer<List<SessionWithStreamsDBObject>> { dbSessions ->
        DatabaseProvider.runQuery { coroutineScope ->
            val sessions = dbSessions.map { dbSession -> Session(dbSession) }
            val sensorThresholds = getSensorThresholds(sessions)

            if (anySessionChanged(sessions) || anySensorThresholdChanged(sensorThresholds)) {
                hideLoader(coroutineScope)

                if (sessions.size > 0) {
                    updateSensorThresholds(sensorThresholds)
                    showSessionsView(coroutineScope, sessions)
                } else {
                    showEmptyView(coroutineScope)
                }

                updateSessionsCache(sessions)
            }
        }
    }

    private fun hideLoader(coroutineScope: CoroutineScope) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc.hideLoader()
        }
    }

    private fun showSessionsView(coroutineScope: CoroutineScope, sessions: List<Session>) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc.showSessionsView(sessions, mSensorThresholds)
        }
    }

    private fun showEmptyView(coroutineScope: CoroutineScope) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc.showEmptyView()
        }
    }

    private fun getSensorThresholds(sessions: List<Session>): List<SensorThreshold> {
        val streams = sessions.flatMap { it.streams }.distinctBy { it.sensorName }
        return mSessionsViewModel.findOrCreateSensorThresholds(streams)
    }

    private fun anySensorThresholdChanged(sensorThresholds: List<SensorThreshold>): Boolean {
        return mSensorThresholds.isEmpty() ||
                sensorThresholds.any { threshold -> threshold.hasChangedFrom(mSensorThresholds[threshold.sensorName]) }
    }

    private fun updateSensorThresholds(sensorThresholds: List<SensorThreshold>) {
        sensorThresholds.forEach { mSensorThresholds[it.sensorName] = it }
    }

    private fun anySessionChanged(sessions: List<Session>): Boolean {
        return mSessions.isEmpty() ||
                mSessions.size != sessions.size ||
                sessions.any { session -> session.hasChangedFrom(mSessions[session.uuid]) }
    }

    private fun updateSessionsCache(sessions: List<Session>) {
        sessions.forEach { session -> mSessions[session.uuid] = session }
    }

    fun registerSessionsObserver() {
        mSessionsLiveData.observe(mLifecycleOwner, mSessionsObserver)
    }

    fun unregisterSessionsObserver() {
        mSessionsLiveData.removeObserver(mSessionsObserver)
    }

    abstract fun loadSessions(): LiveData<List<SessionWithStreamsDBObject>>

    fun onCreate() {
        mViewMvc.showLoader()
    }

    fun onResume() {
        registerSessionsObserver()
        mViewMvc.registerListener(this)
    }

    fun onPause() {
        unregisterSessionsObserver()
        mViewMvc.unregisterListener(this)
    }

    protected fun startNewSession(sessionType: Session.Type) {
        NewSessionActivity.start(mRootActivity, sessionType)
    }

    override fun onSwipeToRefreshTriggered() {
        mMobileSessionsSyncService.sync({
            mViewMvc.showLoader()
        }, {
            mViewMvc.hideLoader()
        })
    }

    override fun onFollowButtonClicked(session: Session) {
        updateFollowedAt(session)
        NavigationController.goToDashboard(DashboardPagerAdapter.FOLLOWING_TAB_INDEX)
    }

    override fun onUnfollowButtonClicked(session: Session) {
        updateFollowedAt(session)
    }

    private fun updateFollowedAt(session: Session) {
        DatabaseProvider.runQuery {
            mSessionsViewModel.updateFollowedAt(session)
        }
    }

    override fun onMapButtonClicked(session: Session, sensorName: String?) {
        MapActivity.start(mRootActivity, sensorName, session.uuid, session.type.value, session.status.value)
    }

    override fun onExpandSessionCard(session: Session) {
        if (session.isIncomplete()) {
            mViewMvc.showLoaderFor(session)
            val finallyCallback = { mViewMvc.hideLoaderFor(session) }
            mDownloadMeasurementsService.downloadMeasurements(session, finallyCallback)
        }
    }
}

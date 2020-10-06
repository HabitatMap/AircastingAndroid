package io.lunarlogic.aircasting.screens.dashboard

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.DownloadMeasurementsService
import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.screens.map.MapActivity
import io.lunarlogic.aircasting.sensor.Session

abstract class SessionsController(
    private val mRootActivity: FragmentActivity?,
    private val mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    private val mLifecycleOwner: LifecycleOwner,
    private val mSettings: Settings
) : SessionsViewMvc.Listener {
    private val mErrorHandler = ErrorHandler(mRootActivity!!)
    private val mApiService =  ApiServiceFactory.get(mSettings.getAuthToken()!!)
    private val mMobileSessionsSyncService = SessionsSyncService(mApiService, mErrorHandler)
    private val mDownloadMeasurementsService = DownloadMeasurementsService(mApiService, mErrorHandler)

    protected lateinit var mSessionsLiveData: LiveData<List<SessionWithStreamsDBObject>>
    private var mSessions = hashMapOf<String, Session>()

    private var mSessionsObserver = Observer<List<SessionWithStreamsDBObject>> { dbSessions ->
        val sessions = dbSessions.map { dbSession -> Session(dbSession) }

        if (anySessionChanged(sessions)) {
            mViewMvc.hideLoader()

            if (sessions.size > 0) {
                mViewMvc.showSessionsView(sessions)
            } else {
                mViewMvc.showEmptyView()
            }

            updateSessionsCache(sessions)
        }
    }

    private fun anySessionChanged(sessions: List<Session>): Boolean {
        return mSessions.isEmpty() || sessions.any { session -> session.hasChangedFrom(mSessions[session.uuid]) }
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

    override fun onSwipeToRefreshTriggered(callback: () -> Unit) {
        mMobileSessionsSyncService.sync(callback)
    }

    override fun onMapButtonClicked(sessionUUID: String, sensorName: String?) {
        MapActivity.start(mRootActivity, sessionUUID, sensorName)
    }

    override fun onExpandSessionCard(session: Session) {
        if (session.isIncomplete()) {
            mViewMvc.showLoaderFor(session)
            val finallyCallback = { mViewMvc.hideLoaderFor(session) }
            mDownloadMeasurementsService.downloadMeasurements(session, finallyCallback)
        }
    }
}

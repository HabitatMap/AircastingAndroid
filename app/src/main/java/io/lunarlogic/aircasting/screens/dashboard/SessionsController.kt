package io.lunarlogic.aircasting.screens.dashboard

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
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

    fun registerSessionsObserver() {
        loadSessions().observe(mLifecycleOwner, Observer { sessions ->
            if (sessions.size > 0) {
                mViewMvc.showSessionsView(sessions.map { session ->
                    Session(session)
                })
            } else {
                mViewMvc.showEmptyView()
            }
        })
    }

    abstract fun loadSessions(): LiveData<List<SessionWithStreamsDBObject>>

    protected fun startNewSession(sessionType: Session.Type) {
        NewSessionActivity.start(mRootActivity, sessionType)
    }

    override fun onSwipeToRefreshTriggered(callback: () -> Unit) {
        mMobileSessionsSyncService.sync(callback)
    }

    override fun onMapButtonClicked(sessionUUID: String, sensorName: String?) {
        MapActivity.start(mRootActivity, sessionUUID, sensorName)
    }

    override fun onExpandSessionCard(sessionUUID: String) {
        // TODO: download session data
    }
}

package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.MobileSessionsSyncService
import io.lunarlogic.aircasting.sensor.Session

abstract class SessionsController(
    private val mContext: Context?,
    private val mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    private val mLifecycleOwner: LifecycleOwner,
    private val mSettings: Settings
) : SessionsViewMvc.Listener {
    private val mErrorHandler = ErrorHandler(mContext!!)
    private val mApiService =  ApiServiceFactory.get(mSettings.getAuthToken()!!)
    private val mMobileSessionsSyncService = MobileSessionsSyncService(mApiService, mErrorHandler)

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
        NewSessionActivity.start(mContext, sessionType)
    }

    override fun onSwipeToRefreshTriggered(callback: () -> Unit) {
        mMobileSessionsSyncService.sync(callback)
    }
}

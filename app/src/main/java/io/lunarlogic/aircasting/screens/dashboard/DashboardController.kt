package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.events.DeleteSessionEvent
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.SyncService
import io.lunarlogic.aircasting.sensor.Session
import org.greenrobot.eventbus.EventBus

class DashboardController(
    private val mContext: Context?,
    private val mViewMvc: DashboardViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    private val mLifecycleOwner: LifecycleOwner
) : DashboardViewMvc.Listener {
    val mSettings = Settings(mContext!!)
    val apiService =  ApiServiceFactory.get(mSettings.getAuthToken()!!)
    val sessionSyncService = SyncService(apiService, ErrorHandler(mContext!!))

    fun onCreate() {
        mSessionsViewModel.loadAllWithMeasurements().observe(mLifecycleOwner, Observer { sessions ->
            if (sessions.size > 0) {
                mViewMvc.showSessionsView(sessions.map { session ->
                    Session(session)
                })
            } else {
                mViewMvc.showEmptyView()
            }
        })

        mViewMvc.registerListener(this)
    }

    fun onDestroy() {
        mViewMvc.unregisterListener(this)
    }

    override fun onRecordNewSessionClicked() {
        NewSessionActivity.start(mContext)
    }

    override fun onStopSessionClicked(sessionUUID: String) {
        val event = StopRecordingEvent(sessionUUID)
        EventBus.getDefault().post(event)
    }

    override fun onDeleteSessionClicked(sessionUUID: String) {
        val event = DeleteSessionEvent(sessionUUID)
        EventBus.getDefault().post(event)
    }

    override fun onSwipeToRefreshTriggered(callback: () -> Unit) {
        sessionSyncService.sync(callback)
    }
}
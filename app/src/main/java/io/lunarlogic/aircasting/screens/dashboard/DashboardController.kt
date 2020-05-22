package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.sensor.Session
import org.greenrobot.eventbus.EventBus
import java.util.*

class DashboardController(
    private val mContext: Context?,
    private val mViewMvc: DashboardViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    private val mLifecycleOwner: LifecycleOwner
) : DashboardViewMvc.Listener {

    fun onCreate() {
        mSessionsViewModel.loadAllWithMeasurements().observe(mLifecycleOwner, Observer { sessions ->
            if (sessions.size > 0) {
                mViewMvc.showSessionsView(sessions.map { session -> Session(session) })
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
}
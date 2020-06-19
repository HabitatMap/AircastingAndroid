package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
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

    fun onCreate() {
        mViewMvc.registerListener(this)
    }

    fun onDestroy() {
        mViewMvc.unregisterListener(this)
    }
}
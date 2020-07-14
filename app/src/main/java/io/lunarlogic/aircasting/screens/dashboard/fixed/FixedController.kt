package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.events.DeleteSessionEvent
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.dashboard.SessionsController
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc
import org.greenrobot.eventbus.EventBus

class FixedController(
    private val mContext: Context?,
    private val mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    private val mLifecycleOwner: LifecycleOwner,
    mSettings: Settings
): SessionsController(mContext, mViewMvc, mSessionsViewModel, mLifecycleOwner, mSettings),
    SessionsViewMvc.Listener {

    override fun loadSessions(): LiveData<List<SessionWithStreamsDBObject>> {
        return mSessionsViewModel.loadFixedSessionsWithMeasurements()
    }

    fun onCreate() {
        registerSessionsObserver()
        mViewMvc.registerListener(this)
    }

    fun onDestroy() {
        mViewMvc.unregisterListener(this)
    }

    override fun onDeleteSessionClicked(sessionUUID: String) {
        val event = DeleteSessionEvent(sessionUUID)
        EventBus.getDefault().post(event)
    }

    override fun onStopSessionClicked(sessionUUID: String) {
        // do nothing
    }
}
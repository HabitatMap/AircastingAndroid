package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.events.DeleteSessionEvent
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.SyncService
import io.lunarlogic.aircasting.sensor.Session
import org.greenrobot.eventbus.EventBus

class MobileDormantController(
    private val mContext: Context?,
    private val mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    private val mLifecycleOwner: LifecycleOwner
): SessionsController(mContext, mViewMvc, mSessionsViewModel, mLifecycleOwner), SessionsViewMvc.Listener {

    override fun loadSessions(): LiveData<List<SessionWithStreamsDBObject>> {
        return mSessionsViewModel.loadDormantSessionsWithMeasurements()
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
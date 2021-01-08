package io.lunarlogic.aircasting.screens.dashboard.fixed

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.events.DeleteSessionEvent
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.observers.DormantSessionsObserver
import io.lunarlogic.aircasting.screens.dashboard.SessionsController
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.dashboard.DeleteSessionBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.EditSessionBottomSheet
import org.greenrobot.eventbus.EventBus

class FixedController(
    mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    fragmentManager: FragmentManager
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings, mApiServiceFactory, fragmentManager),
    SessionsViewMvc.Listener, EditSessionBottomSheet.Listener {

    private var mSessionsObserver = DormantSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadFixedSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun forceSessionsObserverRefresh() {
        mSessionsObserver.forceRefresh()
    }

    override fun onRecordNewSessionClicked() {
        startNewSession(Session.Type.FIXED)
    }

    override fun onDeleteStreamsPressed(sessionUUID: String) {
        val allStreamsBoxSelected = deleteSessionDialog?.allStreamsBoxSelected()
        if (allStreamsBoxSelected == true) {
            deleteSession(sessionUUID)
        } else  {
            val selectedOptions = deleteSessionDialog?.getSelectedValues()
            deleteStreams(sessionUUID, selectedOptions)
        }
    }

    private fun deleteStreams(sessionUUID: String, selectedOptions: ArrayList<DeleteSessionBottomSheet.Option>?) {
        // TODO
    }

    private fun deleteSession(sessionUUID: String) {
         val event = DeleteSessionEvent(sessionUUID)
         EventBus.getDefault().post(event)
    }

    override fun onStopSessionClicked(sessionUUID: String) {
        // do nothing
    }
}

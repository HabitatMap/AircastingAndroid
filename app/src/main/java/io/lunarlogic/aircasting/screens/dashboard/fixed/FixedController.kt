package io.lunarlogic.aircasting.screens.dashboard.fixed

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.events.DeleteSessionEvent
import io.lunarlogic.aircasting.events.EditSessionEvent
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.observers.DormantSessionsObserver
import io.lunarlogic.aircasting.screens.dashboard.SessionsController
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.dashboard.EditSessionBottomSheet
import org.greenrobot.eventbus.EventBus

class FixedController(
    mRootActivity: FragmentActivity?,
    mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val fragmentManager: FragmentManager
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings, mApiServiceFactory),
    SessionsViewMvc.Listener, EditSessionBottomSheet.Listener {

    private var mSessionsObserver = DormantSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)
    private var dialog: EditSessionBottomSheet? = null


    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadFixedSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onRecordNewSessionClicked() {
        startNewSession(Session.Type.FIXED)
    }
    override fun onEditSessionClicked(session: Session) {
        startEditSessionBottomSheet(session)
    }

    override fun onDeleteSessionClicked(sessionUUID: String) {
        val event = DeleteSessionEvent(sessionUUID)
        EventBus.getDefault().post(event)
    }

    override fun onStopSessionClicked(sessionUUID: String) {
        // do nothing
    }

    private fun startEditSessionBottomSheet(session: Session) {
        dialog = EditSessionBottomSheet(this, session)
        dialog?.show(fragmentManager, "FixedEdit")
    }

    override fun onEditDataPressed() { // handling buttons in EditSessionBottomSheet
        val editData = dialog?.editDataConfirmed()
        editSessionEventPost(editData?.first, editData?.second, editData?.third)
        dialog?.dismiss()
    }

    override fun onCancelPressed() { // handling buttons in EditSessionBottomSheet
        dialog?.dismiss()
    }

    fun editSessionEventPost(sessionId: String?, sessionName: String?, tags: ArrayList<String>?){
        val event = EditSessionEvent(sessionId, sessionName, tags)
        EventBus.getDefault().post(event)
    }


}

package io.lunarlogic.aircasting.screens.dashboard.active

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.events.NoteCreatedEvent
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.lib.NavigationController
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.models.Note
import io.lunarlogic.aircasting.models.observers.ActiveSessionsObserver
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.dashboard.DashboardPagerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsController
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc
import io.lunarlogic.aircasting.screens.sync.SyncActivity
import io.lunarlogic.aircasting.sensor.AirBeamReconnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MobileActiveController(
    private val mRootActivity: FragmentActivity?,
    private val mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector,
    private val mContext: Context
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings, mApiServiceFactory, mRootActivity!!.supportFragmentManager, mContext),
    SessionsViewMvc.Listener,
    AddNoteBottomSheet.Listener {

    private var mSessionsObserver = ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadMobileActiveSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().safeRegister(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onRecordNewSessionClicked() {
        startNewSession(Session.Type.MOBILE)
    }

    override fun onEditSessionClicked(session: Session) {
        // do nothing
    }

    override fun onShareSessionClicked(session: Session) {
        // do nothing
    }

    override fun onDeleteStreamsPressed(session: Session) {
        // do nothing
    }

    override fun onExpandSessionCard(session: Session) {
        // do nothing
    }

    override fun onDisconnectSessionClicked(session: Session) {
        airBeamReconnector.disconnect(session)
    }

    override fun addNoteClicked(session: Session) {
        //todo: dismiss the main BottomSheet <?>
        AddNoteBottomSheet(this, session, mContext).show(fragmentManager)
    }

    override fun onReconnectSessionClicked(session: Session) {
        mViewMvc.showReconnectingLoaderFor(session)
        airBeamReconnector.reconnect(session,
            errorCallback = {
                GlobalScope.launch(Dispatchers.Main) {
                    mErrorHandler.showError(R.string.errors_airbeam_connection_failed)
                }
            },
            finallyCallback = {
                GlobalScope.launch(Dispatchers.Main) {
                    mViewMvc.hideReconnectingLoaderFor(session)
                }
            }
        )
    }

    override fun onEditDataPressed(session: Session, name: String, tags: ArrayList<String>) { // Edit session bottom sheet handling
        // do nothing
    }

    override fun onFinishSessionConfirmed(session: Session) {
        val event = StopRecordingEvent(session.uuid)
        EventBus.getDefault().post(event)

        goToDormantTab()
    }

    override fun onFinishAndSyncSessionConfirmed(session: Session) {
        SyncActivity.start(mRootActivity, onFinish = { goToDormantTab() })
    }

    override fun addNotePressed(session: Session, note: Note) {
        val event = NoteCreatedEvent(note)
        EventBus.getDefault().post(event)
    }
    
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        val deviceId = event.deviceId ?: return
        
        mViewMvc.hideLoaderFor(deviceId)
    }

    private fun goToDormantTab() {
        val tabId = DashboardPagerAdapter.tabIndexForSessionType(
            Session.Type.MOBILE,
            Session.Status.FINISHED
        )
        NavigationController.goToDashboard(tabId)
    }
}

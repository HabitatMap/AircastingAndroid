package io.lunarlogic.aircasting.screens.dashboard.active

import android.app.AlertDialog
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.lib.NavigationController
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.models.observers.ActiveSessionsObserver
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.dashboard.DashboardPagerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsController
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvc
import io.lunarlogic.aircasting.sensor.AirBeamReconnector
import io.lunarlogic.aircasting.sensor.AirBeamSyncService
import io.lunarlogic.aircasting.sensor.airbeam3.sync.SyncEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MobileActiveController(
    mRootActivity: FragmentActivity?,
    private val mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector,
    private val mContext: Context
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings, mApiServiceFactory, mRootActivity!!.supportFragmentManager, mContext),
    SessionsViewMvc.Listener {

    private var mSessionsObserver = ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)
    private var syncProgressDialog: AlertDialog? = null // TODO: remove it after implementing proper sync UI

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

    override fun onReconnectSessionClicked(session: Session) {
        mViewMvc.showReconnectingLoaderFor(session)
        airBeamReconnector.reconnect(session) {
            GlobalScope.launch(Dispatchers.Main) {
                mViewMvc.hideReconnectingLoaderFor(session)
            }
        }
    }

    override fun onEditDataPressed(session: Session, name: String, tags: ArrayList<String>) { // Edit session bottom sheet handling
        // do nothing
    }

    override fun onFinishSessionConfirmed(session: Session) {
        val event = StopRecordingEvent(session.uuid)
        EventBus.getDefault().post(event)

        val tabId = DashboardPagerAdapter.tabIndexForSessionType(
            Session.Type.MOBILE,
            Session.Status.FINISHED
        )
        NavigationController.goToDashboard(tabId)
    }

    override fun onFinishAndSyncSessionConfirmed(session: Session) {
        AirBeamSyncService.startService(mContext)

        syncProgressDialog = AlertDialog.Builder(mContext)
            .setCancelable(false)
            .setPositiveButton("Ok", null)
            .setMessage("Sync started")
            .show()
    }

    // TODO: remove this method after implementing proper sync UI
    @Subscribe
    fun onMessageEvent(event: SyncEvent) {
        syncProgressDialog?.setMessage(event.message)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        mViewMvc.hideLoaderFor(event.deviceId!!)
    }
}

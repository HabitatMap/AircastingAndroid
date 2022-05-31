package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.MobileNavigationDirections
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.observers.ActiveSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsController
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.sync.SyncActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncUnavailableDialog
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.events.NoteCreatedEvent
import pl.llp.aircasting.util.events.StandaloneModeEvent
import pl.llp.aircasting.util.events.StopRecordingEvent
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import pl.llp.aircasting.util.isSDKLessOrEqualToNMR1
import pl.llp.aircasting.util.safeRegister

class MobileActiveController(
    private val mRootActivity: FragmentActivity?,
    private val mViewMvc: SessionsViewMvc?,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector,
    private val mContext: Context
): SessionsController(mRootActivity, mViewMvc, mSessionsViewModel, mSettings, mApiServiceFactory, mRootActivity!!.supportFragmentManager, mContext),
    SessionsViewMvc.Listener,
    AddNoteBottomSheet.Listener,
    AirBeamReconnector.Listener {

    private var mSessionsObserver = ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun onCreate() {
        super.onCreate()
        airBeamReconnector.registerListener(this)
    }

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
        startNewSession(LocalSession.Type.MOBILE)
    }

    override fun onExploreNewSessionsClicked() {
        // Do nothing
    }

    override fun onEditSessionClicked(localSession: LocalSession) {
        // do nothing
    }

    override fun onShareSessionClicked(localSession: LocalSession) {
        // do nothing
    }

    override fun onDeleteStreamsPressed(localSession: LocalSession) {
        // do nothing
    }

    override fun onExpandSessionCard(localSession: LocalSession) {
        // do nothing
    }

    override fun onDisconnectSessionClicked(localSession: LocalSession) {
        if (isSDKLessOrEqualToNMR1()) {
            SyncUnavailableDialog(this.fragmentManager)
                .show()
        } else {
            EventBus.getDefault().post(StandaloneModeEvent(localSession.uuid))
            airBeamReconnector.disconnect(localSession)
        }
    }

    override fun addNoteClicked(localSession: LocalSession) {
        AddNoteBottomSheet(this, localSession, mContext, mErrorHandler).show(fragmentManager)
    }

    override fun onReconnectSessionClicked(localSession: LocalSession) {
        mViewMvc?.showReconnectingLoaderFor(localSession)
        airBeamReconnector.reconnect(localSession,
            deviceItem = null,
            errorCallback = { errorCallback() },
            finallyCallback = { finallyCallback(localSession) }
        )
    }

    override fun onEditDataPressed(localSession: LocalSession, name: String, tags: ArrayList<String>) { // Edit session bottom sheet handling
        // do nothing
    }

    override fun onFinishSessionConfirmed(localSession: LocalSession) {
        val event = StopRecordingEvent(localSession.uuid)
        EventBus.getDefault().post(event)
        goToDormantTab()
    }

    override fun onFinishAndSyncSessionConfirmed(localSession: LocalSession) {
        SyncActivity.start(mRootActivity)
    }

    override fun addNotePressed(localSession: LocalSession, note: Note) {
        val event = NoteCreatedEvent(localSession, note)
        EventBus.getDefault().post(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        val deviceId = event.deviceId ?: return
        
        mViewMvc?.hideLoaderFor(deviceId)
    }

    private fun goToDormantTab() {
        mRootActivity?.let { val action =
            MobileNavigationDirections.actionGlobalDashboard(SessionsTab.MOBILE_DORMANT.value)
            Navigation.findNavController(it, R.id.nav_host_fragment)
                .navigate(action) }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun beforeReconnection(localSession: LocalSession) {
        GlobalScope.launch(Dispatchers.Main) {
            mViewMvc?.showReconnectingLoaderFor(localSession)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun errorCallback() {
        GlobalScope.launch(Dispatchers.Main) {
            mErrorHandler.showError(R.string.errors_airbeam_connection_failed)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun finallyCallback(localSession: LocalSession) {
        GlobalScope.launch(Dispatchers.Main) {
            mViewMvc?.hideReconnectingLoaderFor(localSession)
        }
    }
}

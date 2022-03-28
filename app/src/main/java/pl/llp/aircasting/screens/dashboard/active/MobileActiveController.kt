package pl.llp.aircasting.screens.dashboard.active

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.MobileNavigationDirections
import pl.llp.aircasting.R
import pl.llp.aircasting.events.NewMeasurementEvent
import pl.llp.aircasting.events.NoteCreatedEvent
import pl.llp.aircasting.events.StandaloneModeEvent
import pl.llp.aircasting.events.StopRecordingEvent
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.models.Note
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.models.observers.ActiveSessionsObserver
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.screens.dashboard.SessionsController
import pl.llp.aircasting.screens.dashboard.SessionsTab
import pl.llp.aircasting.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.screens.sync.SyncActivity
import pl.llp.aircasting.screens.sync.SyncUnavailableDialog
import pl.llp.aircasting.sensor.AirBeamReconnector

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
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N_MR1) {
            SyncUnavailableDialog(this.fragmentManager)
                .show()
        } else {
            EventBus.getDefault().post(StandaloneModeEvent(session.uuid))
            airBeamReconnector.disconnect(session)
        }
    }

    override fun addNoteClicked(session: Session) {
        AddNoteBottomSheet(this, session, mContext, mErrorHandler).show(fragmentManager)
    }

    override fun onReconnectSessionClicked(session: Session) {
        mViewMvc?.showReconnectingLoaderFor(session)
        airBeamReconnector.reconnect(session,
            deviceItem = null,
            errorCallback = { errorCallback() },
            finallyCallback = { finallyCallback(session) }
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
        SyncActivity.start(mRootActivity)
    }

    override fun addNotePressed(session: Session, note: Note) {
        val event = NoteCreatedEvent(session, note)
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

    override fun beforeReconnection(session: Session) {
        GlobalScope.launch(Dispatchers.Main) {
            mViewMvc?.showReconnectingLoaderFor(session)
        }
    }

    override fun errorCallback() {
        GlobalScope.launch(Dispatchers.Main) {
            mErrorHandler.showError(R.string.errors_airbeam_connection_failed)
        }
    }

    override fun finallyCallback(session: Session) {
        GlobalScope.launch(Dispatchers.Main) {
            mViewMvc?.hideReconnectingLoaderFor(session)
        }
    }
}

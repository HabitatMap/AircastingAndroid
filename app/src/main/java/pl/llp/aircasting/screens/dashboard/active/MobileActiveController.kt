package pl.llp.aircasting.screens.dashboard.active

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import pl.llp.aircasting.R
import pl.llp.aircasting.events.NewMeasurementEvent
import pl.llp.aircasting.events.NoteCreatedEvent
import pl.llp.aircasting.events.StopRecordingEvent
import pl.llp.aircasting.lib.NavigationController
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.models.Note
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.models.observers.MobileActiveSessionsObserver
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.screens.dashboard.DashboardPagerAdapter
import pl.llp.aircasting.screens.dashboard.SessionsController
import pl.llp.aircasting.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.screens.sync.SyncActivity
import pl.llp.aircasting.sensor.AirBeamReconnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.events.SensorDisconnectedEvent

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

    private var mSessionsObserver = MobileActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

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
        airBeamReconnector.disconnect(session)
    }

    override fun addNoteClicked(session: Session) {
        AddNoteBottomSheet(this, session, mContext, mErrorHandler).show(fragmentManager)
    }

    override fun onReconnectSessionClicked(session: Session) {
        mViewMvc?.showReconnectingLoaderFor(session)
        airBeamReconnector.reconnect(session,
            errorCallback = {
                GlobalScope.launch(Dispatchers.Main) {
                    mErrorHandler.showError(R.string.errors_airbeam_connection_failed)
                }
            },
            finallyCallback = {
                GlobalScope.launch(Dispatchers.Main) {
                    mViewMvc?.hideReconnectingLoaderFor(session)
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
        val event = StopRecordingEvent(session.uuid)
        EventBus.getDefault().post(event)
        SyncActivity.start(mRootActivity, onFinish = { goToDormantTab() })
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
        val tabId = DashboardPagerAdapter.tabIndexForSessionType(
            Session.Type.MOBILE,
            Session.Status.FINISHED
        )
        NavigationController.goToDashboard(tabId)
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

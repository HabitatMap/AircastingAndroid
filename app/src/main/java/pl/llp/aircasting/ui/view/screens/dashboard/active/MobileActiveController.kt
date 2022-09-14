package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.ActiveSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsController
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.view.screens.sync.SyncActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncUnavailableDialog
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.events.NoteCreatedEvent
import pl.llp.aircasting.util.events.StandaloneModeEvent
import pl.llp.aircasting.util.events.StopRecordingEvent
import pl.llp.aircasting.util.extensions.expandedCards
import pl.llp.aircasting.util.extensions.goToDormantTab
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import pl.llp.aircasting.util.isSDKLessOrEqualToNMR1

class MobileActiveController(
    private val mRootActivity: FragmentActivity?,
    private val mViewMvc: SessionsViewMvc?,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector,
    private val permissionsManager: PermissionsManager,
    private val mContext: Context
) : SessionsController(
    mRootActivity,
    mViewMvc,
    mSessionsViewModel,
    mSettings,
    mApiServiceFactory,
    mRootActivity!!.supportFragmentManager,
    mContext
),
    SessionsViewMvc.Listener,
    AddNoteBottomSheet.Listener,
    AirBeamReconnector.Listener {

    private var mSessionsObserver =
        ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

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

    override fun onExploreNewSessionsClicked() {
        // Do nothing
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
        super.onExpandSessionCard(session)
        expandedCards()?.add(session.uuid)
    }

    override fun onCollapseSessionCard(session: Session) {
        expandedCards()?.remove(session.uuid)
    }

    override fun onDisconnectSessionClicked(session: Session) {
        if (isSDKLessOrEqualToNMR1()) {
            SyncUnavailableDialog(this.fragmentManager)
                .show()
        } else {
            EventBus.getDefault().post(StandaloneModeEvent(session.uuid))
            airBeamReconnector.disconnect(session)
        }
    }

    override fun addNoteClicked(session: Session) {
        AddNoteBottomSheet(this, session, mContext, mErrorHandler, permissionsManager).show(
            fragmentManager
        )
    }

    override fun onReconnectSessionClicked(session: Session) {
        mViewMvc?.showReconnectingLoaderFor(session)
        airBeamReconnector.reconnect(session,
            deviceItem = null,
            errorCallback = { errorCallback() },
            finallyCallback = { finallyCallback(session) }
        )
    }

    override fun onEditDataPressed(
        session: Session,
        name: String,
        tags: ArrayList<String>
    ) { // Edit session bottom sheet handling
        // do nothing
    }

    override fun onFinishSessionConfirmed(session: Session) {
        val event = StopRecordingEvent(session.uuid)
        EventBus.getDefault().post(event)

        mSettings.decreaseActiveMobileSessionsNumber()
        val getActiveMobileSessions = mSettings.mobileActiveSessionsCount()
        if (getActiveMobileSessions == 1) goToDormantTab()
    }

    override fun onFinishAndSyncSessionConfirmed(session: Session) {
        mSettings.decreaseActiveMobileSessionsNumber()
        SyncActivity.start(mRootActivity)
    }

    override fun addNotePressed(session: Session, note: Note) {
        val event = NoteCreatedEvent(session, note)
        EventBus.getDefault().post(event)
    }

    override fun showCameraHelperDialog() {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        val deviceId = event.deviceId ?: return

        mViewMvc?.hideLoaderFor(deviceId)
    }

    private fun goToDormantTab() {
        mRootActivity?.goToDormantTab()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun beforeReconnection(session: Session) {
        GlobalScope.launch(Dispatchers.Main) {
            mViewMvc?.showReconnectingLoaderFor(session)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun errorCallback() {
        GlobalScope.launch(Dispatchers.Main) {
            mErrorHandler.showError(R.string.errors_airbeam_connection_failed)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun finallyCallback(session: Session) {
        GlobalScope.launch(Dispatchers.Main) {
            mViewMvc?.hideReconnectingLoaderFor(session)
        }
    }
}

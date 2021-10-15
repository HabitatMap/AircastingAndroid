package pl.llp.aircasting.screens.session_view.map

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.events.*
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.location.LocationHelper
import pl.llp.aircasting.models.Note
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.active.AddNoteBottomSheet
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.screens.session_view.SessionDetailsViewController
import pl.llp.aircasting.screens.session_view.SessionDetailsViewMvc
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.lib.AppBar
import pl.llp.aircasting.sensor.AirBeamReconnector


class MapController(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    sensorName: String?,
    fragmentManager: FragmentManager,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector
): SessionDetailsViewController(rootActivity, mSessionsViewModel, mViewMvc, sessionUUID, sensorName, fragmentManager, mSettings, mApiServiceFactory),
    SessionDetailsViewMvc.Listener,
    AddNoteBottomSheet.Listener {
    private var mLocateRequested = false

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocationChanged) {
        if (mLocateRequested) {
            val location = LocationHelper.lastLocation()
            location?.let { mViewMvc?.centerMap(location) }
            mLocateRequested = false
        }
    }

    override fun onResume() {
        super.onResume()
        mViewMvc?.registerListener(this)
        AppBar.adjustMenuVisibility(false)
    }

    open fun onPause() {
        mViewMvc?.unregisterListener(this)
    }

    override fun locateRequested() {
        val location = LocationHelper.lastLocation()
        if (location == null) {
            requestLocation()
        } else {
            mViewMvc?.centerMap(location)
        }
    }

    private fun requestLocation() {
        mLocateRequested = true
        LocationHelper.checkLocationServicesSettings(rootActivity)
    }

    fun onLocationSettingsSatisfied() {
        LocationHelper.start()
    }

    override fun addNoteClicked(session: Session) {
        AddNoteBottomSheet(this, session, rootActivity, mErrorHandler).show(fragmentManager)
    }

    override fun onFinishSessionConfirmed(session: Session) {
        val event = StopRecordingEvent(session.uuid)
        EventBus.getDefault().post(event)
        rootActivity.finish()
    }

    override fun onSessionDisconnectClicked(session: Session) {
        EventBus.getDefault().post(StandaloneModeEvent(session.uuid))
        airBeamReconnector.disconnect(session)
        rootActivity.finish()
    }

    override fun addNotePressed(session: Session, note: Note) {
        val event = NoteCreatedEvent(session, note)
        EventBus.getDefault().post(event)
        mViewMvc?.addNote(note)
    }

    override fun deleteNotePressed(note: Note?, session: Session?) { // Delete session on EditNoteBottomSheet pressed
        super.deleteNotePressed(note, session)
        if (note != null) {
            mViewMvc?.deleteNote(note)
        }
    }

}

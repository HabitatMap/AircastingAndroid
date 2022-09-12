package pl.llp.aircasting.ui.view.screens.session_view.map

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.active.AddNoteBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.active.CameraPermissionHelperDialog
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewController
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.LocationChanged
import pl.llp.aircasting.util.events.NoteCreatedEvent
import pl.llp.aircasting.util.events.StandaloneModeEvent
import pl.llp.aircasting.util.events.StopRecordingEvent
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

open class MapController(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    sensorName: String?,
    val mFragmentManager: FragmentManager,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector,
    private val permissionsManager: PermissionsManager
) : SessionDetailsViewController(
    rootActivity,
    mSessionsViewModel,
    mViewMvc,
    sessionUUID,
    sensorName,
    mFragmentManager,
    mSettings,
    mApiServiceFactory
),
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
        AddNoteBottomSheet(this, session, rootActivity, mErrorHandler, permissionsManager).show(
            fragmentManager
        )
    }

    override fun onFinishSessionConfirmed(session: Session) {
        val event = StopRecordingEvent(session.uuid)
        EventBus.getDefault().post(event)

        mSettings.decreaseActiveMobileSessionsNumber()
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

    override fun showCameraHelperDialog() {
        CameraPermissionHelperDialog(mFragmentManager) {
            permissionsManager.requestCameraPermission(
                rootActivity
            )
        }.show()
    }

    override fun deleteNotePressed(
        note: Note?,
        session: Session?
    ) { // Delete session on EditNoteBottomSheet pressed
        super.deleteNotePressed(note, session)
        if (note != null) {
            mViewMvc?.deleteNote(note)
        }
    }
}

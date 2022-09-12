package pl.llp.aircasting.ui.view.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.active.AddNoteBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.active.CameraPermissionHelperDialog
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewController
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NoteCreatedEvent
import pl.llp.aircasting.util.events.StandaloneModeEvent
import pl.llp.aircasting.util.events.StopRecordingEvent
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

open class GraphController(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    sensorName: String?,
    fragmentManager: FragmentManager,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector,
    private val permissionsManager: PermissionsManager
) : SessionDetailsViewController(
    rootActivity,
    mSessionsViewModel,
    mViewMvc,
    sessionUUID,
    sensorName,
    fragmentManager,
    mSettings,
    mApiServiceFactory
),
    SessionDetailsViewMvc.Listener,
    AddNoteBottomSheet.Listener {
    override fun locateRequested() {}

    override fun onResume() {
        super.onResume()
        mViewMvc?.registerListener(this)
    }

    open fun onPause() {
        mViewMvc?.unregisterListener(this)
    }

    override fun addNoteClicked(session: Session) {
        AddNoteBottomSheet(this, session, rootActivity, mErrorHandler, permissionsManager).show(
            fragmentManager
        )
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
    }

    override fun showCameraHelperDialog() {
        CameraPermissionHelperDialog(fragmentManager) {
            permissionsManager.requestCameraPermission(
                rootActivity
            )
        }.show()
    }
}

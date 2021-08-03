package pl.llp.aircasting.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.events.NoteCreatedEvent
import pl.llp.aircasting.events.StopRecordingEvent
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.models.Note
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.screens.dashboard.active.AddNoteBottomSheet
import pl.llp.aircasting.screens.session_view.SessionDetailsViewController
import pl.llp.aircasting.screens.session_view.SessionDetailsViewMvc
import org.greenrobot.eventbus.EventBus


class GraphController(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    sensorName: String?,
    fragmentManager: FragmentManager,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory
): SessionDetailsViewController(rootActivity, mSessionsViewModel, mViewMvc, sessionUUID, sensorName, fragmentManager, mSettings, mApiServiceFactory),
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
        AddNoteBottomSheet(this, session, rootActivity, mErrorHandler).show(fragmentManager) //todo: add rootActivity to AddNoteBottomSheet constructor so we can ask for permission there
    }

    override fun onFinishSessionConfirmed(session: Session) {
        val event = StopRecordingEvent(session.uuid)
        EventBus.getDefault().post(event)
        rootActivity.finish()
    }

    override fun addNotePressed(session: Session, note: Note) {
        val event = NoteCreatedEvent(session, note)
        EventBus.getDefault().post(event)
    }
}

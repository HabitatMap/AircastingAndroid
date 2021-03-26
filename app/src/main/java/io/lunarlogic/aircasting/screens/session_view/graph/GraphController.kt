package io.lunarlogic.aircasting.screens.session_view.graph

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.events.NoteCreatedEvent
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.dashboard.active.AddNoteBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.active.EditNoteBottomSheet
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewController
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import org.greenrobot.eventbus.EventBus


class GraphController(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc,
    sessionUUID: String,
    sensorName: String?,
    val fragmentManager: FragmentManager
): SessionDetailsViewController(rootActivity, mSessionsViewModel, mViewMvc, sessionUUID, sensorName),
    SessionDetailsViewMvc.Listener,
    AddNoteBottomSheet.Listener,
    EditNoteBottomSheet.Listener {
    override fun locateRequested() {}

    open fun onResume() {
        mViewMvc.registerListener(this)
    }

    open fun onPause() {
        mViewMvc.unregisterListener(this)
    }

    override fun addNoteClicked(session: Session) {
        AddNoteBottomSheet(this, session, rootActivity).show(fragmentManager)
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

    override fun editNoteClicked(markerId: String) { // how to deal with these to Edit functions below ???
        EditNoteBottomSheet(this, markerId).show(fragmentManager)
    }

    override fun editNotePressed(markerId: String) {
        EditNoteBottomSheet(this, markerId).show(fragmentManager) //todo: note.text just temporary i need to pass sessionId here somehow ??
//        TODO("Not yet implemented")
    }

    override fun deleteNotePressed(note: Note) {
        TODO("Not yet implemented")
    }
}

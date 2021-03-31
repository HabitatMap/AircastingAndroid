package io.lunarlogic.aircasting.screens.session_view.map

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.database.repositories.NoteRepository
import io.lunarlogic.aircasting.events.LocationChanged
import io.lunarlogic.aircasting.events.NoteCreatedEvent
import io.lunarlogic.aircasting.events.NoteEditedEvent
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.screens.dashboard.active.AddNoteBottomSheet
import io.lunarlogic.aircasting.screens.dashboard.active.EditNoteBottomSheet
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewController
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MapController(
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
    private var mLocateRequested = false


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocationChanged) {
        if (mLocateRequested) {
            val location = LocationHelper.lastLocation()
            location?.let { mViewMvc.centerMap(location) }
            mLocateRequested = false
        }
    }

    open fun onResume() {
        mViewMvc.registerListener(this)
    }

    open fun onPause() {
        mViewMvc.unregisterListener(this)
    }

    override fun locateRequested() {
        val location = LocationHelper.lastLocation()
        if (location == null) {
            requestLocation()
        } else {
            mViewMvc.centerMap(location)
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
        mViewMvc.addNote(note)
    }

    override fun noteMarkerClicked(session: Session?, noteNumber: Int) {
        EditNoteBottomSheet(this, session, noteNumber).show(fragmentManager)
    }

    override fun saveChangesNotePressed(note: Note?, session: Session?) { // buttons from edit note bottom sheet
        TODO("Not yet implemented, NotEditedEvent to be sent here <?>")
//        val event = NoteEditedEvent(note, session)
//        EventBus.getDefault().post(event)
    }

    override fun deleteNotePressed(note: Note?) { // Delete session on EditNoteBottomSheet pressed
        TODO("Not yet implemented, NoteDeletedEvent to be sent here <?>")
    }
}

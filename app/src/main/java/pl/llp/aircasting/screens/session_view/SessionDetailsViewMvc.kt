package pl.llp.aircasting.screens.session_view

import android.location.Location
import pl.llp.aircasting.models.Measurement
import pl.llp.aircasting.models.Note
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.ObservableViewMvc
import pl.llp.aircasting.screens.dashboard.SessionPresenter
import pl.llp.aircasting.screens.session_view.hlu.HLUListener

interface SessionDetailsViewMvc: ObservableViewMvc<SessionDetailsViewMvc.Listener> {
    fun addMeasurement(measurement: Measurement)
    fun bindSession(sessionPresenter: SessionPresenter?)
    fun refreshStatisticsContainer()
    fun centerMap(location: Location)
    fun onDestroy()
    fun addNote(note: Note)
    fun deleteNote(note: Note)
    fun getSessionType(): Session.Type

    interface Listener: HLUListener {
        fun locateRequested()
        fun addNoteClicked(session: Session)
        fun noteMarkerClicked(session: Session?, noteNumber: Int)
        fun onFinishSessionConfirmed(session: Session)
        fun refreshSession()
    }
}

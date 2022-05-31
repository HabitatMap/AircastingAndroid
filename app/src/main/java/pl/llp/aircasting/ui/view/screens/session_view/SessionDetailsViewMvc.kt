package pl.llp.aircasting.ui.view.screens.session_view

import android.location.Location
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.common.ObservableViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.session_view.hlu.HLUListener

interface SessionDetailsViewMvc: ObservableViewMvc<SessionDetailsViewMvc.Listener> {
    fun addMeasurement(measurement: Measurement)
    fun bindSession(sessionPresenter: SessionPresenter?)
    fun refreshStatisticsContainer()
    fun centerMap(location: Location)
    fun onDestroy()
    fun addNote(note: Note)
    fun deleteNote(note: Note)
    fun getSessionType(): LocalSession.Type

    interface Listener: HLUListener {
        fun locateRequested()
        fun addNoteClicked(localSession: LocalSession)
        fun noteMarkerClicked(localSession: LocalSession?, noteNumber: Int)
        fun onFinishSessionConfirmed(localSession: LocalSession)
        fun onSessionDisconnectClicked(localSession: LocalSession)
        fun refreshSession()
    }
}

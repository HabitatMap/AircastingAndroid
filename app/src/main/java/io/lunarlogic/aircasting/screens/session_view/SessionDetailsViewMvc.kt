package io.lunarlogic.aircasting.screens.session_view

import android.location.Location
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.session_view.hlu.HLUListener

interface SessionDetailsViewMvc: ObservableViewMvc<SessionDetailsViewMvc.Listener> {
    fun addMeasurement(sessionPresenter: SessionPresenter?, measurement: Measurement)
    fun centerMap(location: Location)
    fun onDestroy()

    interface Listener: HLUListener {
        fun locateRequested()
        fun addNoteClicked(session: Session)
        fun onFinishSessionConfirmed(session: Session)
    }
}

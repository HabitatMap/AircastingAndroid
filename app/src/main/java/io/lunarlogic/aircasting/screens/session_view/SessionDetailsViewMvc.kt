package io.lunarlogic.aircasting.screens.session_view

import android.location.Location
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.session_view.hlu.HLUListener

interface SessionDetailsViewMvc: ObservableViewMvc<SessionDetailsViewMvc.Listener> {
    fun bindSession(sessionPresenter: SessionPresenter?)

    fun addMeasurement(measurement: Measurement)
    fun addFixedMeasurements()
    fun centerMap(location: Location)

    interface Listener: HLUListener {
        fun locateRequested()
    }
}

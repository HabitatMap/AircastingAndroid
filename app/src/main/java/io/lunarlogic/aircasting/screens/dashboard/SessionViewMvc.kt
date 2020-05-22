package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session

interface SessionViewMvc: ObservableViewMvc<SessionViewMvc.Listener> {
    interface Listener {
        fun onSessionStopClicked(session: Session)
    }
    fun bindSession(session: Session)
}
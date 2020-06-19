package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.sensor.Session

interface ActiveSessionViewMvc: SessionViewMvc<ActiveSessionViewMvc.Listener> {
    interface Listener {
        fun onSessionStopClicked(session: Session)
    }
}
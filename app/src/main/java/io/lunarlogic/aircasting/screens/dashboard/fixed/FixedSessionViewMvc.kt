package io.lunarlogic.aircasting.screens.dashboard.fixed

import io.lunarlogic.aircasting.screens.dashboard.SessionCardListener
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc
import io.lunarlogic.aircasting.sensor.Session

interface FixedSessionViewMvc:
    SessionViewMvc<FixedSessionViewMvc.Listener> {
    interface Listener: SessionCardListener {
        fun onSessionDeleteClicked(session: Session)
    }
}

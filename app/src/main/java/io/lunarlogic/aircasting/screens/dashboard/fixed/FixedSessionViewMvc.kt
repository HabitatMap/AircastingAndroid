package io.lunarlogic.aircasting.screens.dashboard.fixed

import io.lunarlogic.aircasting.screens.dashboard.SessionCardListener
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc
import io.lunarlogic.aircasting.models.Session

interface FixedSessionViewMvc:
    SessionViewMvc<FixedSessionViewMvc.Listener> {
    interface Listener: SessionCardListener {
        fun onSessionEditClicked(session: Session)
        fun onSessionShareClicked(session: Session)
        fun onSessionDeleteClicked(session: Session)
    }
}

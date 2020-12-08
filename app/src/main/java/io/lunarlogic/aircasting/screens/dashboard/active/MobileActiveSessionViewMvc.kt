package io.lunarlogic.aircasting.screens.dashboard.active

import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.SessionCardListener
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc

interface MobileActiveSessionViewMvc:
    SessionViewMvc<MobileActiveSessionViewMvc.Listener> {
    interface Listener: SessionCardListener {
        fun onSessionStopClicked(session: Session)
    }
}

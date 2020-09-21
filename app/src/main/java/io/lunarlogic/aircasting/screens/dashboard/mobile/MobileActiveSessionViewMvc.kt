package io.lunarlogic.aircasting.screens.dashboard.mobile

import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc
import io.lunarlogic.aircasting.sensor.Session

interface MobileActiveSessionViewMvc:
    SessionViewMvc<MobileActiveSessionViewMvc.Listener> {
    interface Listener {
        fun onSessionStopClicked(session: Session)
        fun onMapButtonClicked(session: Session)
    }
}

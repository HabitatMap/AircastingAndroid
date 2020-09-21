package io.lunarlogic.aircasting.screens.dashboard.mobile

import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc
import io.lunarlogic.aircasting.sensor.Session

interface MobileDormantSessionViewMvc:
    SessionViewMvc<MobileDormantSessionViewMvc.Listener> {
    interface Listener {
        fun onSessionDeleteClicked(session: Session)
        fun onMapButtonClicked(session: Session)
    }
}

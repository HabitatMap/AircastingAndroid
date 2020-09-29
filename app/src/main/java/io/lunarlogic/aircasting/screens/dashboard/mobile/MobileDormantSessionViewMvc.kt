package io.lunarlogic.aircasting.screens.dashboard.mobile

import io.lunarlogic.aircasting.screens.dashboard.SessionCardListener
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc
import io.lunarlogic.aircasting.sensor.Session

interface MobileDormantSessionViewMvc:
    SessionViewMvc<MobileDormantSessionViewMvc.Listener> {
    interface Listener: SessionCardListener {
        fun onSessionDeleteClicked(session: Session)
    }
}

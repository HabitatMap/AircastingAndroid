package io.lunarlogic.aircasting.screens.dashboard.dormant

import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.SessionCardListener
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc

interface MobileDormantSessionViewMvc:
    SessionViewMvc<MobileDormantSessionViewMvc.Listener> {
    interface Listener: SessionCardListener {
        fun onSessionEditClicked(session: Session)
        fun onSessionShareClicked(session: Session)
        fun onSessionDeleteClicked(session: Session)
    }
}

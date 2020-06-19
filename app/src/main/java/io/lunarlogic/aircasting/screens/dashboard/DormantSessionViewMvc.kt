package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.sensor.Session

interface DormantSessionViewMvc: SessionViewMvc<DormantSessionViewMvc.Listener> {
    interface Listener {
        fun onSessionDeleteClicked(session: Session)
    }
}
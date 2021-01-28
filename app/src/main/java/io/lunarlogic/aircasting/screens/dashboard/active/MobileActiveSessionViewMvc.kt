package io.lunarlogic.aircasting.screens.dashboard.active

import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.SessionCardListener
import io.lunarlogic.aircasting.screens.dashboard.SessionViewMvc

interface MobileActiveSessionViewMvc:
    SessionViewMvc<MobileActiveSessionViewMvc.Listener> {

    interface DisconnectedViewListener: FinishSessionListener {
        fun onSessionReconnectClicked(session: Session)
//        fun onSessionStopClicked(session: Session)
    }

    interface Listener: SessionCardListener, DisconnectedViewListener {
        fun onSessionDisconnectClicked(session: Session)
    }
}

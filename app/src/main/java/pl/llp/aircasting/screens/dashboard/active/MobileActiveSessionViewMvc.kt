package pl.llp.aircasting.screens.dashboard.active

import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.SessionViewMvc

interface MobileActiveSessionViewMvc:
    SessionViewMvc<MobileActiveSessionViewMvc.Listener> {

    interface DisconnectedViewListener: FinishSessionListener {
        fun onSessionReconnectClicked(session: Session)
    }

    interface Listener: SessionCardListener, DisconnectedViewListener {
        fun onSessionDisconnectClicked(session: Session)
        fun addNoteClicked(session: Session)
    }
}

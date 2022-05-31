package pl.llp.aircasting.ui.view.screens.dashboard.active

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvc

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

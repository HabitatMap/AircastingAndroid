package pl.llp.aircasting.ui.view.screens.dashboard.active

import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvc

interface MobileActiveSessionViewMvc:
    SessionViewMvc<MobileActiveSessionViewMvc.Listener> {

    interface DisconnectedViewListener: FinishSessionListener {
        fun onSessionReconnectClicked(localSession: LocalSession)
    }

    interface Listener: SessionCardListener, DisconnectedViewListener {
        fun onSessionDisconnectClicked(localSession: LocalSession)
        fun addNoteClicked(localSession: LocalSession)
    }
}

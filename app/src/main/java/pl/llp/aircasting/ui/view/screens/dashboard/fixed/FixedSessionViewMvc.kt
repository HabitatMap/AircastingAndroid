package pl.llp.aircasting.ui.view.screens.dashboard.fixed

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvc

interface FixedSessionViewMvc:
    SessionViewMvc<FixedSessionViewMvc.Listener> {
    interface Listener: SessionCardListener {
        fun onSessionEditClicked(session: Session)
        fun onSessionShareClicked(session: Session)
        fun onSessionDeleteClicked(session: Session)
    }
}

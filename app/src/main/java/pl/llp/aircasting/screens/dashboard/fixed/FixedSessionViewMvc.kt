package pl.llp.aircasting.screens.dashboard.fixed

import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.SessionViewMvc

interface FixedSessionViewMvc:
    SessionViewMvc<FixedSessionViewMvc.Listener> {
    interface Listener: SessionCardListener {
        fun onSessionEditClicked(session: Session)
        fun onSessionShareClicked(session: Session)
        fun onSessionDeleteClicked(session: Session)
    }
}

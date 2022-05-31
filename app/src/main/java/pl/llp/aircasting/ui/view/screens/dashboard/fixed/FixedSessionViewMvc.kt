package pl.llp.aircasting.ui.view.screens.dashboard.fixed

import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvc

interface FixedSessionViewMvc:
    SessionViewMvc<FixedSessionViewMvc.Listener> {
    interface Listener: SessionCardListener {
        fun onSessionEditClicked(localSession: LocalSession)
        fun onSessionShareClicked(localSession: LocalSession)
        fun onSessionDeleteClicked(localSession: LocalSession)
    }
}

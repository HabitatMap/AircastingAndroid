package pl.llp.aircasting.ui.view.screens.dashboard.dormant

import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvc

interface MobileDormantSessionViewMvc:
    SessionViewMvc<MobileDormantSessionViewMvc.Listener> {
    interface Listener: SessionCardListener {
        fun onSessionEditClicked(localSession: LocalSession)
        fun onSessionShareClicked(localSession: LocalSession)
        fun onSessionDeleteClicked(localSession: LocalSession)
    }
}

package pl.llp.aircasting.ui.view.screens.dashboard.dormant

import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvc

interface MobileDormantSessionViewMvc:
    SessionViewMvc<MobileDormantSessionViewMvc.Listener> {
    interface Listener: SessionCardListener
}

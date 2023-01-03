package pl.llp.aircasting.ui.view.screens.dashboard.active

import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionViewMvc

interface MobileActiveSessionViewMvc:
    SessionViewMvc<MobileActiveSessionViewMvc.Listener> {

    interface Listener: SessionCardListener
}

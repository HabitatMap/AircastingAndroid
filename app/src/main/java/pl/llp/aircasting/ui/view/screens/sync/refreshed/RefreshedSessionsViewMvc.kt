package pl.llp.aircasting.ui.view.screens.sync.refreshed

import pl.llp.aircasting.ui.view.screens.common.ObservableViewMvc

interface RefreshedSessionsViewMvc: ObservableViewMvc<RefreshedSessionsViewMvc.Listener> {
    interface Listener {
        fun refreshedSessionsContinueClicked()
        fun refreshedSessionsRetryClicked()
        fun refreshedSessionsCancelClicked()
    }
}

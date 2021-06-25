package pl.llp.aircasting.screens.sync.refreshed

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface RefreshedSessionsViewMvc: ObservableViewMvc<RefreshedSessionsViewMvc.Listener> {
    interface Listener {
        fun refreshedSessionsContinueClicked()
        fun refreshedSessionsRetryClicked()
        fun refreshedSessionsCancelClicked()
    }
}

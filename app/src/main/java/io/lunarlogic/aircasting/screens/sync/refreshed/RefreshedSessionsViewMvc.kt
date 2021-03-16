package io.lunarlogic.aircasting.screens.sync.refreshed

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface RefreshedSessionsViewMvc: ObservableViewMvc<RefreshedSessionsViewMvc.Listener> {
    interface Listener {
        fun refreshedSessionsContinueClicked()
        fun refreshedSessionsRetryClicked()
        fun refreshedSessionsCancelClicked()
    }
}

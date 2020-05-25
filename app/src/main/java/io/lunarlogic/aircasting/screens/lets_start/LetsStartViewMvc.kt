package io.lunarlogic.aircasting.screens.lets_start

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface LetsStartViewMvc: ObservableViewMvc<LetsStartViewMvc.Listener> {
    interface Listener {
        fun onRecordNewSessionClicked()
    }
}
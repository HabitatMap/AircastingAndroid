package io.lunarlogic.aircasting.screens.sync.error

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface ErrorViewMvc: ObservableViewMvc<ErrorViewMvc.Listener> {
    interface Listener {
        fun onErrorViewOkClicked()
    }
}

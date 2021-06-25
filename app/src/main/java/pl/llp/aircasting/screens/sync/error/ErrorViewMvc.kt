package pl.llp.aircasting.screens.sync.error

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface ErrorViewMvc: ObservableViewMvc<ErrorViewMvc.Listener> {
    interface Listener {
        fun onErrorViewOkClicked()
    }
}

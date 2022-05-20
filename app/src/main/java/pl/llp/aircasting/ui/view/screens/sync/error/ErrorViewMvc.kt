package pl.llp.aircasting.ui.view.screens.sync.error

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface ErrorViewMvc: ObservableViewMvc<ErrorViewMvc.Listener> {
    interface Listener {
        fun onErrorViewOkClicked()
    }
}

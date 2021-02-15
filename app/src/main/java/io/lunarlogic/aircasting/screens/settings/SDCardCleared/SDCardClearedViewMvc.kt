package io.lunarlogic.aircasting.screens.settings.SDCardCleared

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface SDCardClearedViewMvc: ObservableViewMvc<SDCardClearedViewMvc.Listener> {
    interface Listener {
        fun onSDCardClearedConfirmationClicked()
    }
}

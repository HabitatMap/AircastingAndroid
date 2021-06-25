package pl.llp.aircasting.screens.settings.clear_sd_card.sd_card_cleared

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface SDCardClearedViewMvc: ObservableViewMvc<SDCardClearedViewMvc.Listener> {
    interface Listener {
        fun onSDCardClearedConfirmationClicked()
    }
}

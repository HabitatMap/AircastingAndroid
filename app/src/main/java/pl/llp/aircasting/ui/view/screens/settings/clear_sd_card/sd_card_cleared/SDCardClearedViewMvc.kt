package pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.sd_card_cleared

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface SDCardClearedViewMvc: ObservableViewMvc<SDCardClearedViewMvc.Listener> {
    interface Listener {
        fun onSDCardClearedConfirmationClicked()
    }
}

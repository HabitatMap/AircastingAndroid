package pl.llp.aircasting.ui.view.screens.sync.confirmation

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface AirbeamSyncConfirmationViewMvc : ObservableViewMvc<AirbeamSyncConfirmationViewMvc.Listener> {
    interface Listener {
        fun onYesClicked()
        fun onNoClicked()
    }
}

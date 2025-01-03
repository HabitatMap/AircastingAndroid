package pl.llp.aircasting.ui.view.screens.sync.synced

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface AirbeamSyncedViewMvc: ObservableViewMvc<AirbeamSyncedViewMvc.Listener> {
    interface Listener {
        fun onAirbeamSyncedContinueClicked()
    }
}

package pl.llp.aircasting.screens.sync.synced

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface AirbeamSyncedViewMvc: ObservableViewMvc<AirbeamSyncedViewMvc.Listener> {
    interface Listener {
        fun onAirbeamSyncedContinueClicked()
    }
}

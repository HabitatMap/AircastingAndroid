package io.lunarlogic.aircasting.screens.sync.synced

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface AirbeamSyncedViewMvc: ObservableViewMvc<AirbeamSyncedViewMvc.Listener> {
    interface Listener {
        fun onAirbeamSyncedContinueClicked()
    }
}

package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface AirBeamConnectedViewMvc : ObservableViewMvc<AirBeamConnectedViewMvc.Listener> {
    interface Listener {
        fun onAirBeamConnectedContinueClicked(sessionUUID: String)
    }
}
package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface TurnOnAirBeamViewMvc : ObservableViewMvc<TurnOnAirBeamViewMvc.Listener> {
    interface Listener {
        fun onReadyClicked()
    }
}
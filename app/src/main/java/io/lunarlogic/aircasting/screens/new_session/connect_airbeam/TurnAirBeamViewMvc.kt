package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface TurnAirBeamViewMvc : ObservableViewMvc<TurnAirBeamViewMvc.Listener> {
    interface Listener {
        fun onReadyClicked()
    }
}
package io.lunarlogic.aircasting.screens.settings.restart_airbeam

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface RestartAirBeamViewMvc : ObservableViewMvc<RestartAirBeamViewMvc.Listener> {
    interface Listener {
        fun onTurnOnAirBeamReadyClicked()
    }
}

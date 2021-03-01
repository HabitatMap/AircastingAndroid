package io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface RestartAirBeamViewMvc : ObservableViewMvc<RestartAirBeamViewMvc.Listener> {
    interface Listener {
        fun onTurnOnAirBeamReadyClicked()
    }
}

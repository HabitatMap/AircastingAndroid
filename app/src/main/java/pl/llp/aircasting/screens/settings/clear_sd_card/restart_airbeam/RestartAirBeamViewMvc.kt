package pl.llp.aircasting.screens.settings.clear_sd_card.restart_airbeam

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface RestartAirBeamViewMvc : ObservableViewMvc<RestartAirBeamViewMvc.Listener> {
    interface Listener {
        fun onTurnOnAirBeamReadyClicked()
    }
}

package pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.restart_airbeam

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface RestartAirBeamViewMvc : ObservableViewMvc<RestartAirBeamViewMvc.Listener> {
    interface Listener {
        fun onTurnOnAirBeamReadyClicked()
    }
}

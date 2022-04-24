package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import pl.llp.aircasting.ui.view.screens.common.ObservableViewMvc

interface TurnOnAirBeamViewMvc : ObservableViewMvc<TurnOnAirBeamViewMvc.Listener> {
    interface Listener {
        fun onTurnOnAirBeamReadyClicked()
    }
}

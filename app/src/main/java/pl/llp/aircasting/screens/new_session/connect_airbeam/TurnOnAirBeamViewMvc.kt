package pl.llp.aircasting.screens.new_session.connect_airbeam

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface TurnOnAirBeamViewMvc : ObservableViewMvc<TurnOnAirBeamViewMvc.Listener> {
    interface Listener {
        fun onTurnOnAirBeamReadyClicked()
    }
}

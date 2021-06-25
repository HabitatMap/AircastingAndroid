package pl.llp.aircasting.screens.new_session.connect_airbeam

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface TurnOnLocationServicesViewMvc: ObservableViewMvc<TurnOnLocationServicesViewMvc.Listener> {
    interface Listener {
        fun onTurnOnLocationServicesOkClicked()
    }
}

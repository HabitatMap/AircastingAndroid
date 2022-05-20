package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface TurnOnLocationServicesViewMvc: ObservableViewMvc<TurnOnLocationServicesViewMvc.Listener> {
    interface Listener {
        fun onTurnOnLocationServicesOkClicked()
    }
}

package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface TurnOnLocationServicesViewMvc: ObservableViewMvc<TurnOnLocationServicesViewMvc.Listener> {
    interface Listener {
        fun onTurnOnLocationServicesOkClicked()
    }
}

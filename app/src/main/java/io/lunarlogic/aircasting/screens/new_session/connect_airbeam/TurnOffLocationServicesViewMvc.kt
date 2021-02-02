package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface TurnOffLocationServicesViewMvc: ObservableViewMvc<TurnOffLocationServicesViewMvc.Listener> {
    interface Listener {
        fun onTurnOffLocationServicesOkClicked()
    }
}

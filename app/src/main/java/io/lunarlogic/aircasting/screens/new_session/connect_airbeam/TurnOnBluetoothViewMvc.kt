package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface TurnOnBluetoothViewMvc : ObservableViewMvc<TurnOnBluetoothViewMvc.Listener> {
    interface Listener {
        fun onTurnOnBluetoothReadyClicked()
    }
}
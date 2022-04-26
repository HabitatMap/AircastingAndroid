package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import pl.llp.aircasting.ui.view.screens.common.ObservableViewMvc

interface TurnOnBluetoothViewMvc : ObservableViewMvc<TurnOnBluetoothViewMvc.Listener> {
    interface Listener {
        fun onTurnOnBluetoothContinueClicked()
    }
}

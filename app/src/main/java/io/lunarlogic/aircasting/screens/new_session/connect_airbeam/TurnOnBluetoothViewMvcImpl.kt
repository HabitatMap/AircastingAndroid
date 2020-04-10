package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class TurnOnBluetoothViewMvcImpl : BaseObservableViewMvc<TurnOnBluetoothViewMvc.Listener>, TurnOnBluetoothViewMvc {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_turn_on_bluetooth, parent, false)
        val button = rootView?.findViewById<Button>(R.id.turn_on_bluetooth_ready_button)
        button?.setOnClickListener {
            onReadyClicked()
        }
    }

    private fun onReadyClicked() {
        for (listener in listeners) {
            listener.onTurnOnBluetoothReadyClicked()
        }
    }
}
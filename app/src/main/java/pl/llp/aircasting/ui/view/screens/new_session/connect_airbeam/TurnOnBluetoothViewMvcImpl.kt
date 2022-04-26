package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.common.BaseObservableViewMvc

class TurnOnBluetoothViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TurnOnBluetoothViewMvc.Listener>(), TurnOnBluetoothViewMvc {

    init {
        this.rootView = inflater.inflate(R.layout.fragment_turn_on_bluetooth, parent, false)
        val button = rootView?.findViewById<Button>(R.id.turn_on_bluetooth_ok_button)
        button?.setOnClickListener {
            onOkClicked()
        }
    }

    private fun onOkClicked() {
        for (listener in listeners) {
            listener.onTurnOnBluetoothContinueClicked()
        }
    }
}

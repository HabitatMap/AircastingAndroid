package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class TurnOnAirBeamViewMvcImpl : BaseObservableViewMvc<TurnOnAirBeamViewMvc.Listener>, TurnOnAirBeamViewMvc {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_turn_airbeam, parent, false)
        val button = rootView?.findViewById<Button>(R.id.turn_on_airbeam_ready_button)
        button?.setOnClickListener {
            onReadyClicked()
        }
    }

    private fun onReadyClicked() {
        for (listener in listeners) {
            listener.onReadyClicked()
        }
    }
}
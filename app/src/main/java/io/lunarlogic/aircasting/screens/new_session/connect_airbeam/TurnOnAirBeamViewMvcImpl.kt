package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session

class TurnOnAirBeamViewMvcImpl : BaseObservableViewMvc<TurnOnAirBeamViewMvc.Listener>, TurnOnAirBeamViewMvc {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?, sessionType: Session.Type): super() {
        val fragment = getFragmentId(sessionType)
        this.rootView = inflater.inflate(fragment, parent, false)
        val button = rootView?.findViewById<Button>(R.id.turn_on_airbeam_ready_button)
        button?.setOnClickListener {
            onReadyClicked()
        }
    }

    private fun onReadyClicked() {
        for (listener in listeners) {
            listener.onTurnOnAirBeamReadyClicked()
        }
    }
    private fun getFragmentId(sessionType: Session.Type): Int {
        return when (sessionType) {
            Session.Type.FIXED -> R.layout.fragment_turn_on_airbeam_fixed
            Session.Type.MOBILE -> R.layout.fragment_turn_on_airbeam_mobile
        }
    }
}

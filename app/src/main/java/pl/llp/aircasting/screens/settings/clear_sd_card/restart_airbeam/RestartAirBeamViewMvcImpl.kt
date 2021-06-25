package pl.llp.aircasting.screens.settings.clear_sd_card.restart_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BaseObservableViewMvc

class RestartAirBeamViewMvcImpl : BaseObservableViewMvc<RestartAirBeamViewMvc.Listener>, RestartAirBeamViewMvc {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_restart_airbeam, parent, false)
        val button = rootView?.findViewById<Button>(R.id.restart_airbeam_ready_button)
        button?.setOnClickListener {
            onReadyClicked()
        }
    }

    private fun onReadyClicked() {
        for (listener in listeners) {
            listener.onTurnOnAirBeamReadyClicked()
        }
    }
}

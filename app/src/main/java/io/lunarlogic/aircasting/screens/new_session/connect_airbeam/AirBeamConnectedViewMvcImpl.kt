package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.common.BaseViewMvc

class AirBeamConnectedViewMvcImpl : BaseObservableViewMvc<AirBeamConnectedViewMvc.Listener>, AirBeamConnectedViewMvc {

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_airbeam_connected, parent, false)
        val button = rootView?.findViewById<Button>(R.id.airbeam_connected_continue_button)
        button?.setOnClickListener {
            onContinueClicked()
        }
    }

    private fun onContinueClicked() {
        for (listener in listeners) {
            listener.onContinueClicked()
        }
    }
}
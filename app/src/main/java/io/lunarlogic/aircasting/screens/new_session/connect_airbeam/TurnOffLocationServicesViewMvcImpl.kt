package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class TurnOffLocationServicesViewMvcImpl: BaseObservableViewMvc<TurnOffLocationServicesViewMvc.Listener>, TurnOffLocationServicesViewMvc {
    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super(){
        this.rootView = inflater.inflate(R.layout.fragment_turn_off_location_services, parent, false)

        val button = rootView?.findViewById<Button>(R.id.turn_on_location_services_ok_button)

        button?.setOnClickListener {
            onOkClicked()
        }
    }

    private fun onOkClicked() {
        for (listener in listeners) {
            listener.onTurnOffLocationServicesOkClicked()
        }
    }
}

package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class TurnOnLocationServicesViewMvcImpl: BaseObservableViewMvc<TurnOnLocationServicesViewMvc.Listener>, TurnOnLocationServicesViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        areMapsDisabled: Boolean): super() {
        this.rootView = inflater.inflate(R.layout.fragment_turn_on_location_services, parent, false)
        val button = rootView?.findViewById<Button>(R.id.turn_on_location_services_ok_button)
        val turnOnLocationTextView = rootView?.findViewById<TextView>(R.id.turn_location_services_description)
        button?.setOnClickListener {
            onOkClicked()
        }
        if (areMapsDisabled) {
            turnOnLocationTextView?.text = "Location services must be turned on to enable Bluetooth scanning. " +
                    "You'll be prompted to turn off location services after you've selected the Bluetooth device you'll be recording with."
        }
    }

    private fun onOkClicked() {
        for (listener in listeners) {
            listener.onTurnOnLocationServicesOkClicked()
        }
    }
}

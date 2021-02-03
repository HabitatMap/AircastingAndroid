package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class TurnOnLocationServicesViewMvcImpl: BaseObservableViewMvc<TurnOnLocationServicesViewMvc.Listener>, TurnOnLocationServicesViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        areMapsDisabled: Boolean,
        sessionType: Session.Type): super() {
        this.rootView = inflater.inflate(R.layout.fragment_turn_on_location_services, parent, false)
        val button = rootView?.findViewById<Button>(R.id.turn_on_location_services_ok_button)
        val turnOnLocationTextView = rootView?.findViewById<TextView>(R.id.turn_on_location_services_description)

        button?.setOnClickListener {
            onOkClicked()
        }

        if (areMapsDisabled && sessionType == Session.Type.MOBILE) {
            turnOnLocationTextView?.text = context.getString(R.string.locations_services_must_be_turned_on)
        }
    }

    private fun onOkClicked() {
        for (listener in listeners) {
            listener.onTurnOnLocationServicesOkClicked()
        }
    }
}

package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.fragment_turn_on_location_services.view.*

class TurnOnLocationServicesViewMvcImpl: BaseObservableViewMvc<TurnOnLocationServicesViewMvc.Listener>, TurnOnLocationServicesViewMvc {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        useDetailedExplanation: Boolean,
        areMapsDisabled: Boolean
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_turn_on_location_services, parent, false)

        val button = rootView?.turn_on_location_services_ok_button
        button?.setOnClickListener {
            onOkClicked()
        }

        if (useDetailedExplanation) {
            val turnOnLocationPart1TextView = rootView?.turn_on_location_services_description_part1
            turnOnLocationPart1TextView?.text = context.getString(R.string.locations_services_must_be_turned_on_part1)
        }

        if (areMapsDisabled) {
            val turnOnLocationPart2TextView = rootView?.turn_on_location_services_description_part2
            turnOnLocationPart2TextView?.text = context.getString(R.string.locations_services_must_be_turned_on_part2)
            turnOnLocationPart2TextView?.visibility = View.VISIBLE
        }
    }

    private fun onOkClicked() {
        for (listener in listeners) {
            listener.onTurnOnLocationServicesOkClicked()
        }
    }
}

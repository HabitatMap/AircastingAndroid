package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.fragment_turn_off_location_services.view.*
import pl.llp.aircasting.data.model.Session

class TurnOffLocationServicesViewMvcImpl: BaseObservableViewMvc<TurnOffLocationServicesViewMvc.Listener>, TurnOffLocationServicesViewMvc {
    var session: Session?

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        session: Session?
    ): super(){
        this.rootView = inflater.inflate(R.layout.fragment_turn_off_location_services, parent, false)

        this.session = session

        val okButton = rootView?.turn_off_location_services_ok_button
        okButton?.setOnClickListener {
            onOkClicked()
        }

        val skipButton = rootView?.turn_off_location_services_skip_button
        skipButton?.setOnClickListener {
            onSkipClicked()
        }
    }

    private fun onOkClicked() {
        for (listener in listeners) {
            listener.onTurnOffLocationServicesOkClicked(session)
        }
    }

    private fun onSkipClicked() {
        for (listener in listeners) {
            listener.onSkipClicked(session)
        }
    }
}

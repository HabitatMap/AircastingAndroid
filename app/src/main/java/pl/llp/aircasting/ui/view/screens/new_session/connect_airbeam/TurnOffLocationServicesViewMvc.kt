package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.common.ObservableViewMvc

interface TurnOffLocationServicesViewMvc: ObservableViewMvc<TurnOffLocationServicesViewMvc.Listener> {
    interface Listener {
        fun onTurnOffLocationServicesOkClicked(session: Session?)
        fun onSkipClicked(session: Session?)
    }
}

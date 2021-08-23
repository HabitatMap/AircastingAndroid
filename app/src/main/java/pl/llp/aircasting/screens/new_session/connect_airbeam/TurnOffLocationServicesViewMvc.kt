package pl.llp.aircasting.screens.new_session.connect_airbeam

import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.ObservableViewMvc
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

interface TurnOffLocationServicesViewMvc: ObservableViewMvc<TurnOffLocationServicesViewMvc.Listener> {
    interface Listener {
        fun onTurnOffLocationServicesOkClicked(session: Session?)
        fun onSkipClicked(session: Session?)
    }
}

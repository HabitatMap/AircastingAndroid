package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import pl.llp.aircasting.ui.view.screens.common.ObservableViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

interface AirBeamConnectedViewMvc : ObservableViewMvc<AirBeamConnectedViewMvc.Listener> {
    interface Listener {
        fun onAirBeamConnectedContinueClicked(deviceItem: DeviceItem, sessionUUID: String)
    }
}

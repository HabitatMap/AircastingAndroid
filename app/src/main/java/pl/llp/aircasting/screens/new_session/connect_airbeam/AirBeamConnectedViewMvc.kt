package pl.llp.aircasting.screens.new_session.connect_airbeam

import pl.llp.aircasting.screens.common.ObservableViewMvc
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

interface AirBeamConnectedViewMvc : ObservableViewMvc<AirBeamConnectedViewMvc.Listener> {
    interface Listener {
        fun onAirBeamConnectedContinueClicked(deviceItem: DeviceItem, sessionUUID: String)
    }
}

package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

interface AirBeamConnectedViewMvc : ObservableViewMvc<AirBeamConnectedViewMvc.Listener> {
    interface Listener {
        fun onAirBeamConnectedContinueClicked(deviceItem: DeviceItem, sessionUUID: String)
    }
}

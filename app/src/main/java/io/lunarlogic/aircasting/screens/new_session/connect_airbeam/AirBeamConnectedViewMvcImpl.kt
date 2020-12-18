package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.common.BaseViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

class AirBeamConnectedViewMvcImpl : BaseObservableViewMvc<AirBeamConnectedViewMvc.Listener>, AirBeamConnectedViewMvc {
    var deviceItem: DeviceItem

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        deviceItem: DeviceItem
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_airbeam_connected, parent, false)
        this.deviceItem = deviceItem
        val button = rootView?.findViewById<Button>(R.id.airbeam_connected_continue_button)
        button?.setOnClickListener {
            onAirBeamConnectedContinueClicked()
        }
    }

    private fun onAirBeamConnectedContinueClicked() {
        for (listener in listeners) {
            listener.onAirBeamConnectedContinueClicked(deviceItem)
        }
    }
}

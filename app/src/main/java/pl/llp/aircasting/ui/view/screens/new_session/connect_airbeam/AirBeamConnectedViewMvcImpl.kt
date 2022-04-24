package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.common.BaseObservableViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

class AirBeamConnectedViewMvcImpl : BaseObservableViewMvc<AirBeamConnectedViewMvc.Listener>, AirBeamConnectedViewMvc {
    var deviceItem: DeviceItem
    var sessionUUID: String

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        deviceItem: DeviceItem,
        sessionUUID: String
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_airbeam_connected, parent, false)
        this.deviceItem = deviceItem
        this.sessionUUID = sessionUUID
        val button = rootView?.findViewById<Button>(R.id.airbeam_connected_continue_button)
        button?.setOnClickListener {
            onAirBeamConnectedContinueClicked()
        }
    }

    private fun onAirBeamConnectedContinueClicked() {
        for (listener in listeners) {
            listener.onAirBeamConnectedContinueClicked(deviceItem, sessionUUID)
        }
    }
}

package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import kotlinx.android.synthetic.main.fragment_turn_off_location_services.view.*

class TurnOffLocationServicesViewMvcImpl: BaseObservableViewMvc<TurnOffLocationServicesViewMvc.Listener>, TurnOffLocationServicesViewMvc {
    var deviceItem: DeviceItem
    var sessionUUID: String

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        deviceItem: DeviceItem,
        sessionUUID: String
    ): super(){
        this.rootView = inflater.inflate(R.layout.fragment_turn_off_location_services, parent, false)

        this.deviceItem = deviceItem
        this.sessionUUID = sessionUUID

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
            listener.onTurnOffLocationServicesOkClicked(sessionUUID, deviceItem)
        }
    }

    private fun onSkipClicked() {
        for (listener in listeners) {
            listener.onSkipClicked(sessionUUID, deviceItem)
        }
    }
}

package io.lunarlogic.aircasting.screens.new_session.select_device

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class SelectDeviceTypeViewMvcImpl : BaseObservableViewMvc<SelectDeviceTypeViewMvc.Listener>, SelectDeviceTypeViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_select_device_type, parent, false)

        val blueToothDeviceCard = rootView?.findViewById<CardView>(R.id.select_device_type_bluetooth_card)
        blueToothDeviceCard?.setOnClickListener {
            onBluetoothDeviceSelected()
        }

        val microphoneDeviceCard = rootView?.findViewById<CardView>(R.id.select_device_type_microphone_card)
        microphoneDeviceCard?.setOnClickListener {
            onMicrophoneDeviceSelected()
        }
    }

    private fun onBluetoothDeviceSelected() {
        for (listener in listeners) {
            listener.onBluetoothDeviceSelected()
        }
    }

    private fun onMicrophoneDeviceSelected() {
        for (listener in listeners) {
            listener.onMicrophoneDeviceSelected()
        }
    }
}

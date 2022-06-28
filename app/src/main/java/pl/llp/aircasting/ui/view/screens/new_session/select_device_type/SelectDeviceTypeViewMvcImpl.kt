package pl.llp.aircasting.ui.view.screens.new_session.select_device_type

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc

class SelectDeviceTypeViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<SelectDeviceTypeViewMvc.Listener>(),
    SelectDeviceTypeViewMvc {
    init {
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

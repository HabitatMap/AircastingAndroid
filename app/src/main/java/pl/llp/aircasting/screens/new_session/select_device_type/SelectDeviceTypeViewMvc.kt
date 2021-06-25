package pl.llp.aircasting.screens.new_session.select_device

import pl.llp.aircasting.screens.common.ObservableViewMvc


interface SelectDeviceTypeViewMvc : ObservableViewMvc<SelectDeviceTypeViewMvc.Listener> {

    interface Listener {
        fun onBluetoothDeviceSelected()
        fun onMicrophoneDeviceSelected()
    }
}

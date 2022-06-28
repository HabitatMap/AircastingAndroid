package pl.llp.aircasting.ui.view.screens.new_session.select_device_type

import pl.llp.aircasting.ui.view.common.ObservableViewMvc


interface SelectDeviceTypeViewMvc : ObservableViewMvc<SelectDeviceTypeViewMvc.Listener> {

    interface Listener {
        fun onBluetoothDeviceSelected()
        fun onMicrophoneDeviceSelected()
    }
}

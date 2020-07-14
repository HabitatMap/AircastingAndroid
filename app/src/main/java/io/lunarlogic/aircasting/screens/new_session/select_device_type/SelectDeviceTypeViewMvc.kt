package io.lunarlogic.aircasting.screens.new_session.select_device

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface SelectDeviceTypeViewMvc : ObservableViewMvc<SelectDeviceTypeViewMvc.Listener> {

    interface Listener {
        fun onBluetoothDeviceSelected()
        fun onMicrophoneDeviceSelected()
    }
}
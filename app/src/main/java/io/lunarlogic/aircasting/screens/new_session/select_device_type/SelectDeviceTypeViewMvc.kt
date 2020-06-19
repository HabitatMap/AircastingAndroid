package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface SelectDeviceTypeViewMvc : ObservableViewMvc<SelectDeviceTypeViewMvc.Listener> {

    interface Listener {
        fun onBluetoothDeviceSelected()
        fun onMicrophoneDeviceSelected()
    }
}
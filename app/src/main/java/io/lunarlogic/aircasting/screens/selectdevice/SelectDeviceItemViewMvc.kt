package io.lunarlogic.aircasting.screens.selectdevice

import io.lunarlogic.aircasting.devices.Device
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface SelectDeviceItemViewMvc : ObservableViewMvc<SelectDeviceViewMvc.Listener> {
    interface Listener {
        fun onQuestionClicked(device: Device)
    }

    abstract fun bindDevice(device: Device)
}
package io.lunarlogic.aircasting.screens.selectdevice

import io.lunarlogic.aircasting.devices.Device
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface SelectDeviceViewMvc : ObservableViewMvc<SelectDeviceViewMvc.Listener> {
    interface Listener {
        fun onDeviceSelected(device: Device)
    }

    fun bindDevices(devices: List<Device>)
}
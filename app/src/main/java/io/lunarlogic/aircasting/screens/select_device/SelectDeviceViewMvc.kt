package io.lunarlogic.aircasting.screens.select_device

import io.lunarlogic.aircasting.devices.Device
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.select_device.items.DeviceItem

interface SelectDeviceViewMvc : ObservableViewMvc<SelectDeviceViewMvc.Listener> {
    interface Listener {
        fun onDeviceItemSelected(deviceItem: DeviceItem)
    }

    fun bindDevices(devices: List<Device>)
}
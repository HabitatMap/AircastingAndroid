package io.lunarlogic.aircasting.screens.new_session.select_device

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface SelectDeviceViewMvc : ObservableViewMvc<SelectDeviceViewMvc.Listener> {
    interface Listener {
        fun onConnectClicked(selectedDeviceItem: DeviceItem)
    }

    fun bindDeviceItems(deviceItems: List<DeviceItem>)
    fun addDeviceItem(deviceItem: DeviceItem)
}

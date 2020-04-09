package io.lunarlogic.aircasting.screens.new_session.select_device.items

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc

interface SelectDeviceItemViewMvc : ObservableViewMvc<SelectDeviceViewMvc.Listener> {
    interface Listener {
        fun onDeviceItemClicked(deviceItem: DeviceItem)
    }

    fun bindDeviceItem(deviceItem: DeviceItem)
}
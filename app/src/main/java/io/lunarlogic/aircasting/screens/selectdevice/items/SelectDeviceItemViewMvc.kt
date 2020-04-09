package io.lunarlogic.aircasting.screens.selectdevice.items

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.selectdevice.SelectDeviceViewMvc

interface SelectDeviceItemViewMvc : ObservableViewMvc<SelectDeviceViewMvc.Listener> {
    interface Listener {
        fun onDeviceItemClicked(deviceItem: DeviceItem)
    }

    fun bindDeviceItem(deviceItem: DeviceItem)
}
package pl.llp.aircasting.ui.view.screens.new_session.select_device

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface SelectDeviceViewMvc : ObservableViewMvc<SelectDeviceViewMvc.Listener> {
    interface Listener {
        fun onConnectClicked(selectedDeviceItem: DeviceItem)
    }

    interface OnRefreshListener {
        fun onRefreshClicked()
    }

    fun registerOnRefreshListener(refreshListener: OnRefreshListener)
    fun bindDeviceItems(deviceItems: List<DeviceItem>)
    fun addDeviceItem(deviceItem: DeviceItem)
    fun clearRecycler()
}

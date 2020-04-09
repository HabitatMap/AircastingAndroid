package io.lunarlogic.aircasting.screens.new_session.select_device.items

import android.view.LayoutInflater
import android.view.ViewGroup

class SelectDeviceItemViewFactory {
    fun get(viewType: Int, inflater: LayoutInflater, parent: ViewGroup): SelectDeviceItemViewMvc? {
        return when (viewType) {
            ADD_NEW_DEVICE_VIEW_TYPE -> SelectDeviceItemViewAddNewMvcImpl(inflater, parent)
            else -> SelectDeviceItemViewAirBeam2MvcImpl(inflater, parent)
        }
    }
}
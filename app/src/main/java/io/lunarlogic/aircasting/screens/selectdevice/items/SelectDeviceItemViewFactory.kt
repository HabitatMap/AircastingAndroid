package io.lunarlogic.aircasting.screens.selectdevice.items

import android.view.LayoutInflater
import android.view.ViewGroup

class SelectDeviceItemViewFactory {
    fun get(viewType: Int, inflater: LayoutInflater, parent: ViewGroup): SelectDeviceItemViewMvc? {
        return when (viewType) {
            0 -> SelectDeviceItemViewAddNewMvcImpl(inflater, parent)
            else -> SelectDeviceItemViewAirBeam2MvcImpl(inflater, parent)
        }
    }
}
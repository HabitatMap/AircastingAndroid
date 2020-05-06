package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.events.NewMeasurementEvent


interface SelectDeviceTypeViewMvc : ObservableViewMvc<SelectDeviceTypeViewMvc.Listener> {

    interface Listener {
        fun onBluetoothDeviceSelected()
    }
}
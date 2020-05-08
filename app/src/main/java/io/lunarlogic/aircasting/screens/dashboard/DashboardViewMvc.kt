package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.sensor.Measurement


interface DashboardViewMvc : ObservableViewMvc<DashboardViewMvc.Listener> {

    interface Listener {
        fun onRecordNewSessionClicked()
        fun onStopSessionClicked()
    }

    fun updateButtons()
    fun updateMeasurements(measurement: Measurement)
}
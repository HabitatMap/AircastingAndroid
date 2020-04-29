package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.sensor.SensorEvent


interface DashboardViewMvc : ObservableViewMvc<DashboardViewMvc.Listener> {

    interface Listener {
        fun onRecordNewSessionClicked()
    }

    fun updateMeasurements(sensorEvent: SensorEvent)
}
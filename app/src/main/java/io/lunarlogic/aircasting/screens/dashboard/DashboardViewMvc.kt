package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface DashboardViewMvc : ObservableViewMvc<DashboardViewMvc.Listener> {

    interface Listener {
        fun onRecordNewSessionClicked()
        fun onStopSessionClicked()
    }

    fun updateMeasurements(measurementEvent: NewMeasurementEvent)
}
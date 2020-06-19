package io.lunarlogic.aircasting.screens.dashboard

import androidx.lifecycle.LiveData
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.sensor.Session
import java.util.*


interface DashboardViewMvc : ObservableViewMvc<DashboardViewMvc.Listener> {
    interface Listener {}
}
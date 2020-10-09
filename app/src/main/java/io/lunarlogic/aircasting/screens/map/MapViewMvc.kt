package io.lunarlogic.aircasting.screens.map

import android.location.Location
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.sensor.Measurement

interface MapViewMvc: ObservableViewMvc<MapViewMvc.Listener> {
    fun bindSession(sessionPresenter: SessionPresenter)
    fun addMeasurement(measurement: Measurement)
    fun centerMap(location: Location)

    interface Listener {
        fun locateRequested()
    }
}

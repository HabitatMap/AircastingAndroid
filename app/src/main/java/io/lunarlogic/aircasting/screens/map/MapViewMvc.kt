package io.lunarlogic.aircasting.screens.map

import android.location.Location
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.SensorThreshold

interface MapViewMvc: ObservableViewMvc<MapViewMvc.Listener> {
    fun bindSession(sessionPresenter: SessionPresenter?)

    fun addMeasurement(measurement: Measurement)
    fun centerMap(location: Location)

    interface Listener {
        fun locateRequested()
        fun onSensorThresholdChanged(sensorThreshold: SensorThreshold)
        fun onHLUDialogValidationFailed()
    }

    interface HLUDialogListener {
        fun onSensorThresholdChangedFromDialog(sensorThreshold: SensorThreshold)
        fun onValidationFailed()
    }
}

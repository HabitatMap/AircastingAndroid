package io.lunarlogic.aircasting.screens.map

import android.location.Location
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.SensorThreshold
import io.lunarlogic.aircasting.sensor.Session

interface MapViewMvc: ObservableViewMvc<MapViewMvc.Listener> {
    fun bindSession(sessionPresenter: SessionPresenter?, onSensorThresholdChanged: (sensorThreshold: SensorThreshold) -> Unit)
    fun addMeasurement(measurement: Measurement)
    fun centerMap(location: Location)

    interface Listener {
        fun locateRequested()
        fun onHLUDialogValidationFailed()
    }

    interface HLUDialogListener {
        fun onSensorThresholdChangedFromDialog(sensorThreshold: SensorThreshold)
        fun onValidationFailed()
    }
}

package io.lunarlogic.aircasting.screens.graph

import android.location.Location
import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.SensorThreshold

interface GraphViewMvc: ObservableViewMvc<GraphViewMvc.Listener> {
    fun bindSession(sessionPresenter: SessionPresenter?, onSensorThresholdChanged: (sensorThreshold: SensorThreshold) -> Unit)

    fun addMeasurement(measurement: Measurement)
    fun centerMap(location: Location)

    interface Listener {
        fun locateRequested()
        fun onMeasurementStreamChanged(measurementStream: MeasurementStream)
        fun onHLUDialogValidationFailed()
    }
}

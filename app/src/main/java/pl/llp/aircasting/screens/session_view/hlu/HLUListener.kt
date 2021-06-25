package pl.llp.aircasting.screens.session_view.hlu

import pl.llp.aircasting.models.SensorThreshold

interface HLUListener {
    fun onSensorThresholdChanged(sensorThreshold: SensorThreshold)
    fun onHLUDialogValidationFailed()
}

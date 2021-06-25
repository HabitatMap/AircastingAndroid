package pl.llp.aircasting.screens.session_view.hlu

import pl.llp.aircasting.models.SensorThreshold

interface HLUDialogListener {
    fun onSensorThresholdChangedFromDialog(sensorThreshold: SensorThreshold)
    fun onValidationFailed()
}

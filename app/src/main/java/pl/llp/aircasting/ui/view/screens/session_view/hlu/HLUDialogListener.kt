package pl.llp.aircasting.ui.view.screens.session_view.hlu

import pl.llp.aircasting.data.model.SensorThreshold

interface HLUDialogListener {
    fun onSensorThresholdChangedFromDialog(sensorThreshold: SensorThreshold)
    fun onValidationFailed()
}

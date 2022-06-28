package pl.llp.aircasting.ui.view.screens.session_view.hlu

import pl.llp.aircasting.data.model.SensorThreshold

interface HLUListener {
    fun onSensorThresholdChanged(sensorThreshold: SensorThreshold)
    fun onHLUDialogValidationFailed()
}

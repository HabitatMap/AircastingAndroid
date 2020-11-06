package io.lunarlogic.aircasting.screens.session_view.hlu

import io.lunarlogic.aircasting.models.SensorThreshold

interface HLUListener {
    fun onSensorThresholdChanged(sensorThreshold: SensorThreshold)
    fun onHLUDialogValidationFailed()
}

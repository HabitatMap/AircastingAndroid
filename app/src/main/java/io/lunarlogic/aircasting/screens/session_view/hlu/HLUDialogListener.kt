package io.lunarlogic.aircasting.screens.session_view.hlu

import io.lunarlogic.aircasting.models.SensorThreshold

interface HLUDialogListener {
    fun onSensorThresholdChangedFromDialog(sensorThreshold: SensorThreshold)
    fun onValidationFailed()
}

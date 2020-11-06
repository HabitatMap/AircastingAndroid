package io.lunarlogic.aircasting.screens.common

import io.lunarlogic.aircasting.models.SensorThreshold

interface HLUListener {
    fun onSensorThresholdChanged(sensorThreshold: SensorThreshold)
    fun onHLUDialogValidationFailed()
}

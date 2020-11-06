package io.lunarlogic.aircasting.screens.common.hlu

import io.lunarlogic.aircasting.models.SensorThreshold

interface HLUListener {
    fun onSensorThresholdChanged(sensorThreshold: SensorThreshold)
    fun onHLUDialogValidationFailed()
}

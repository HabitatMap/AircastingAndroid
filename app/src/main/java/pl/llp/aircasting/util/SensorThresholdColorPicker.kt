package pl.llp.aircasting.util

import pl.llp.aircasting.data.api.response.search.Sensor

class SensorThresholdColorPicker(
    private val value: Double,
    private val sensor: Sensor
) {
    fun getColor():Int {
        return when {
            value >= sensor.thresholdVeryHigh -> MeasurementColor.VERY_HIGH_COLOR
            value >= sensor.thresholdHigh -> MeasurementColor.HIGH_COLOR
            value >= sensor.thresholdMedium -> MeasurementColor.MEDIUM_COLOR
            value >= sensor.thresholdLow -> MeasurementColor.LOW_COLOR
            value >= sensor.thresholdVeryLow -> MeasurementColor.LOW_COLOR
            else -> MeasurementColor.FALLBACK_COLOR
        }
    }
}
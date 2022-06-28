package pl.llp.aircasting.data.model

import pl.llp.aircasting.data.api.response.search.Sensor
import pl.llp.aircasting.data.local.entity.SensorThresholdDBObject

class SensorThreshold(
    val sensorName: String,
    var thresholdVeryLow: Int,
    var thresholdLow: Int,
    var thresholdMedium: Int,
    var thresholdHigh: Int,
    var thresholdVeryHigh: Int
) {
    constructor(sensorThresholdDBObject: SensorThresholdDBObject) : this(
        sensorThresholdDBObject.sensorName,
        sensorThresholdDBObject.thresholdVeryLow,
        sensorThresholdDBObject.thresholdLow,
        sensorThresholdDBObject.thresholdMedium,
        sensorThresholdDBObject.thresholdHigh,
        sensorThresholdDBObject.thresholdVeryHigh
    )

    constructor(sensor: Sensor) : this(
        sensor.sensorName,
        sensor.thresholdVeryLow,
        sensor.thresholdLow,
        sensor.thresholdMedium,
        sensor.thresholdHigh,
        sensor.thresholdVeryHigh
    )

    val from get() = thresholdVeryLow.toFloat()
    val to get() = thresholdVeryHigh.toFloat()

    val levels
        get() = arrayOf(
            thresholdVeryLow,
            thresholdLow,
            thresholdMedium,
            thresholdHigh,
            thresholdVeryHigh
        )

    fun hasChangedFrom(sensorThreshold: SensorThreshold?): Boolean {
        return sensorThreshold?.sensorName != sensorName ||
                sensorThreshold.thresholdVeryLow != thresholdVeryLow ||
                sensorThreshold.thresholdLow != thresholdLow ||
                sensorThreshold.thresholdMedium != thresholdMedium ||
                sensorThreshold.thresholdHigh != thresholdHigh ||
                sensorThreshold.thresholdVeryHigh != thresholdVeryHigh
    }
}

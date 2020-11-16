package io.lunarlogic.aircasting.models

import io.lunarlogic.aircasting.database.data_classes.SensorThresholdDBObject

class SensorThreshold(
    val sensorName: String,
    var thresholdVeryLow: Int,
    var thresholdLow: Int,
    var thresholdMedium: Int,
    var thresholdHigh: Int,
    var thresholdVeryHigh: Int
) {
    constructor(sensorThresholdDBObject: SensorThresholdDBObject): this(
        sensorThresholdDBObject.sensorName,
        sensorThresholdDBObject.thresholdVeryLow,
        sensorThresholdDBObject.thresholdLow,
        sensorThresholdDBObject.thresholdMedium,
        sensorThresholdDBObject.thresholdHigh,
        sensorThresholdDBObject.thresholdVeryHigh
    )

    val from get() = thresholdVeryLow.toFloat()
    val to get() = thresholdVeryHigh.toFloat()

    val levels get() = arrayOf(thresholdVeryLow, thresholdLow, thresholdMedium, thresholdHigh, thresholdVeryHigh)

    fun hasChangedFrom(sensorThreshold: SensorThreshold?): Boolean {
        return sensorThreshold?.sensorName != sensorName ||
                sensorThreshold.thresholdVeryLow != thresholdVeryLow ||
                sensorThreshold.thresholdLow != thresholdLow ||
                sensorThreshold.thresholdMedium != thresholdMedium ||
                sensorThreshold.thresholdHigh != thresholdHigh ||
                sensorThreshold.thresholdVeryHigh != thresholdVeryHigh
    }
}

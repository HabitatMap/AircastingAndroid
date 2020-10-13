package io.lunarlogic.aircasting.sensor

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
}

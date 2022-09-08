package pl.llp.aircasting.util.events

import java.util.*


class NewMeasurementEvent(
    val packageName: String,
    val sensorName: String,
    val measurementType: String,
    val measurementShortType: String,
    val unitName: String,
    val unitSymbol: String,
    val thresholdVeryLow: Int,
    val thresholdLow: Int,
    val thresholdMedium: Int,
    val thresholdHigh: Int,
    val thresholdVeryHigh: Int,
    val measuredValue: Double
) {
    val creationTime = Date().time

    val deviceId get(): String? = packageName.split(':').lastOrNull()

}

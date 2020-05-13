package io.lunarlogic.aircasting.events

import java.util.*


class NewMeasurementEvent(
    val packageName: String,
    val sensorName: String?,
    val measurementType: String?,
    val measurementShortType: String?,
    val unitName: String?,
    val unitSymbol: String?,
    val thresholdVeryLow: Int?,
    val thresholdLow: Int?,
    val thresholdMedium: Int?,
    val thresholdHigh: Int?,
    val thresholdVeryHigh: Int?,
    val measuredValue: Double?
) {
    val creationTime = Date().time

    override fun toString(): String {
        return "NewMeasurementEvent{" +
                "packageName='" + packageName + '\''.toString() +
                ", sensorName='" + sensorName + '\''.toString() +
                ", measurementType='" + measurementType + '\''.toString() +
                ", measurementShortType='" + measurementShortType + '\''.toString() +
                ", unitName='" + unitName + '\''.toString() +
                ", unitSymbol='" + unitSymbol + '\''.toString() +
                ", thresholdVeryLow=" + thresholdVeryLow +
                ", thresholdLow=" + thresholdLow +
                ", thresholdMedium=" + thresholdMedium +
                ", thresholdHigh=" + thresholdHigh +
                ", thresholdVeryHigh=" + thresholdVeryHigh +
                ", measuredValue=" + measuredValue +
                '}'.toString()
    }
}
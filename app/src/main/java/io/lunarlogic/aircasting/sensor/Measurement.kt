package io.lunarlogic.aircasting.sensor

import java.util.*

class Measurement(
    private val mPackageName: String,
    private val mSensorName: String?,
    private val mMeasurementType: String?,
    private val mMeasurementShortType: String?,
    private val mUnitName: String?,
    private val mUnitSymbol: String?,
    private val mThresholdVeryLow: Int?,
    private val mThresholdLow: Int?,
    private val mThresholdMedium: Int?,
    private val mThresholdHigh: Int?,
    private val mThresholdVeryHigh: Int?,
    private val mMeasuredValue: Double?) {

    private var creationTime = Date().time
    private var address = "none"

    val packageName get() = mPackageName
    val sensorName get() = mSensorName
    val measurementType get() = mMeasurementType
    val measurementShortType get() = mMeasurementShortType
    val unitName get() = mUnitName
    val unitSymbol get() = mUnitSymbol
    val thresholdVeryLow get() = mThresholdVeryLow
    val thresholdLow get() = mThresholdLow
    val thresholdMedium get() = mThresholdMedium
    val thresholdHigh get() = mThresholdHigh
    val thresholdVeryHigh get() = mThresholdVeryHigh
    val measuredValue get() = mMeasuredValue

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
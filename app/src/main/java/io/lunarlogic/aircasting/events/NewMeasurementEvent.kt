package io.lunarlogic.aircasting.events

import java.util.*

class NewMeasurementEvent(
    private val packageName: String,
    private val mSensorName: String?,
    private val mMeasurementType: String?,
    private val shortMeasurementType: String?,
    private val unit: String?,
    private val symbol: String?,
    private val veryLow: Int?,
    private val low: Int?,
    private val mid: Int?,
    private val high: Int?,
    private val veryHigh: Int?,
    private val mMeasuredValue: Double?) {

    private var creationTime = Date().time
    private var address = "none"

    val sensorName get() = mSensorName
    val measurementType get() = mMeasurementType
    val measuredValue get() = mMeasuredValue

    fun debug(): String {
        return "NewMeasurementEvent{" +
                "packageName='" + packageName + '\''.toString() +
                ", sensorName='" + sensorName + '\''.toString() +
                ", measurementType='" + measurementType + '\''.toString() +
                ", shortType='" + shortMeasurementType + '\''.toString() +
                ", unit='" + unit + '\''.toString() +
                ", symbol='" + symbol + '\''.toString() +
                ", veryLow=" + veryLow +
                ", low=" + low +
                ", mid=" + mid +
                ", high=" + high +
                ", veryHigh=" + veryHigh +
                ", measuredValue=" + measuredValue +
                '}'.toString()
    }
}
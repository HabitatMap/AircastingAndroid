package io.lunarlogic.aircasting.sensor

import java.util.*

class SensorEvent(
    private val packageName: String,
    private val sensorName: String?,
    private val measurementType: String?,
    private val shortMeasurementType: String?,
    private val unit: String?,
    private val symbol: String?,
    private val veryLow: Int?,
    private val low: Int?,
    private val mid: Int?,
    private val high: Int?,
    private val veryHigh: Int?,
    private val measuredValue: Double?) {

    private var creationTime = Date().time
    private var address = "none"


    fun debug(): String {
        return "SensorEvent{" +
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
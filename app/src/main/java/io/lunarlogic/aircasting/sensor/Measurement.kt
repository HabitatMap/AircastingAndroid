package io.lunarlogic.aircasting.sensor

import java.util.*

class Measurement(
    private var mValue: Double?,
    private var time: Date?) {

    val value get() = mValue

    override fun toString(): String {
        return "MeasurementDBObject{" +
                "value='" + value + '\''.toString() +
                ", time='" + time + '\''.toString() +
                '}'.toString()
    }
}
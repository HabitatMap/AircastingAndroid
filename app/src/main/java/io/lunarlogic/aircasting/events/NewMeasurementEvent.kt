package io.lunarlogic.aircasting.events

import io.lunarlogic.aircasting.sensor.Measurement


class NewMeasurementEvent(private val mMeasurement: Measurement) {
    val measurement: Measurement get() = mMeasurement
}
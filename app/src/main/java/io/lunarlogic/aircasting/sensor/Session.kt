package io.lunarlogic.aircasting.sensor

import java.util.*

class Session(private var mName: String, private var mTags: List<String>) {
    val uuid = UUID.randomUUID()

    val name get() = mName
    val tags get() = mTags
    val startTime = Date()
    private var mEndTime: Date? = null
    val endTime get() = mEndTime

    private var mStreams = hashMapOf<String, MeasurementStream>()
    val streams get() = mStreams

    fun addMeasurement(measurement: Measurement) {
        measurement.sensorName?.let { sensorName ->
            var stream = mStreams[sensorName]

            if (stream == null) {
                stream = MeasurementStream(measurement)
                mStreams[sensorName] = stream
            }

            stream.addMeasurement(measurement)
        }
    }

    fun stop() {
        mEndTime = Date()
    }
}
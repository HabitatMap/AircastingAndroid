package io.lunarlogic.aircasting.sensor

import java.util.*

class Session(private var mName: String, private var mTags: List<String>) {
    val uuid = UUID.randomUUID()

    val name get() = mName
    val tags get() = mTags
    val notes = listOf<String>() // TODO: change to Note later
    val start_time = Date()
    var end_time: Date? = null

    private var mStreams = hashMapOf<String, MeasurementStream>()

    fun addMeasurement(measurement: Measurement) {
        var stream = mStreams[measurement.sensorName]

        if (stream == null) {
            stream = MeasurementStream(measurement)
            mStreams[stream.sensorName] = stream
        }

        stream.addMeasurement(measurement)
    }
}
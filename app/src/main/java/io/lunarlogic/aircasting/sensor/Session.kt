package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.events.NewMeasurementEvent
import java.util.*

val TAGS_SEPARATOR = " "

class Session(val deviceId: String, private var mName: String, private var mTags: List<String>) {
    val uuid = UUID.randomUUID()

    enum class Status(val value: Int){
        NEW(-1),
        RECORDING(0),
        INTERRUPTED(1),
        FINISHED(2)
    }

    val name get() = mName
    val tags get() = mTags
    val startTime = Date()
    private var mEndTime: Date? = null
    val endTime get() = mEndTime

    private var mStatus: Status = Status.NEW
    val status get() = mStatus

    private var mStreams = hashMapOf<String, MeasurementStream>()
    val streams get() = mStreams

    fun addMeasurement(measurementEvent: NewMeasurementEvent) {
        measurementEvent.sensorName?.let { sensorName ->
            var stream = mStreams[sensorName]

            if (stream == null) {
                stream = MeasurementStream(measurementEvent)
                mStreams[sensorName] = stream
            }

            val measurement = Measurement(measurementEvent.measuredValue, Date(measurementEvent.creationTime))
            stream.addMeasurement(measurement)
        }
    }

    fun startRecording() {
        mStatus = Status.RECORDING
    }

    fun stopRecording() {
        mEndTime = Date()
        mStatus = Status.FINISHED
    }
}
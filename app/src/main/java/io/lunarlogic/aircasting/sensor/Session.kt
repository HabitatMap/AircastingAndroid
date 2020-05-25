package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import java.util.*
import kotlin.collections.ArrayList

val TAGS_SEPARATOR = " "

class Session(
    val uuid: String,
    private var mName: String,
    private var mTags: ArrayList<String>,
    private var mStatus: Status,
    private val mStartTime: Date = Date(),
    private var mEndTime: Date? = null
) {
    constructor(sessionDBObject: SessionDBObject): this(
        sessionDBObject.uuid,
        sessionDBObject.name,
        sessionDBObject.tags,
        sessionDBObject.status,
        sessionDBObject.startTime,
        sessionDBObject.endTime
    )

    constructor(sessionWithStreamsDBObject: SessionWithStreamsDBObject):
            this(sessionWithStreamsDBObject.session) {
        this.mStreams = sessionWithStreamsDBObject.streams.map { streamWithMeasurementsDBObject ->
            MeasurementStream(streamWithMeasurementsDBObject)
        }
    }

    public enum class Status(val value: Int){
        NEW(-1),
        RECORDING(0),
        FINISHED(1)
    }

    val name get() = mName
    val tags get() = mTags
    val startTime get() = mStartTime
    val endTime get() = mEndTime

    val status get() = mStatus

    private var mStreams = listOf<MeasurementStream>()
    val streams get() = mStreams

    fun startRecording() {
        mStatus = Status.RECORDING
    }

    fun stopRecording() {
        mEndTime = Date()
        mStatus = Status.FINISHED
    }

    fun isRecording(): Boolean {
        return status == Status.RECORDING
    }

    companion object {
        fun generateUUID(): String {
            return UUID.randomUUID().toString()
        }
    }
}
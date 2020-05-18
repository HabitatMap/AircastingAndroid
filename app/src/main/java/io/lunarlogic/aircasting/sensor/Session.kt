package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import java.util.*
import kotlin.collections.ArrayList

val TAGS_SEPARATOR = " "

class Session(
    val deviceId: String,
    private var mName: String,
    private var mTags: ArrayList<String>,
    private var mStatus: Status,
    val uuid: String = UUID.randomUUID().toString()
) {
    constructor(sessionDBObject: SessionDBObject): this(
        sessionDBObject.deviceId,
        sessionDBObject.name,
        sessionDBObject.tags,
        sessionDBObject.status,
        sessionDBObject.uuid
    )

    constructor(sessionWithStreamsDBObject: SessionWithStreamsDBObject): this(
        sessionWithStreamsDBObject.session.deviceId,
        sessionWithStreamsDBObject.session.name,
        sessionWithStreamsDBObject.session.tags,
        sessionWithStreamsDBObject.session.status,
        sessionWithStreamsDBObject.session.uuid
    ) {
        this.mStreams = sessionWithStreamsDBObject.streams.map { streamWithMeasurementsDBObject ->
            MeasurementStream(streamWithMeasurementsDBObject)
        }
    }

    public enum class Status(val value: Int){
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

    fun isActive(): Boolean {
        return mStatus != Status.FINISHED
    }
}
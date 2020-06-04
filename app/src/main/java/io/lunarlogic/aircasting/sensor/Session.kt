package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.networking.responses.SessionResponse
import java.util.*
import kotlin.collections.ArrayList

val TAGS_SEPARATOR = " "

class Session(
    val deviceId: String?,
    private var mName: String,
    private var mTags: ArrayList<String>,
    private var mStatus: Status,
    private val mStartTime: Date = Date(),
    private var mEndTime: Date? = null,
    val uuid: String = UUID.randomUUID().toString(),
    var version: Int = 0,
    var deleted: Boolean = false
) {
    constructor(sessionDBObject: SessionDBObject): this(
        sessionDBObject.deviceId,
        sessionDBObject.name,
        sessionDBObject.tags,
        sessionDBObject.status,
        sessionDBObject.startTime,
        sessionDBObject.endTime,
        sessionDBObject.uuid,
        sessionDBObject.version,
        sessionDBObject.deleted
    )

    constructor(sessionWithStreamsDBObject: SessionWithStreamsDBObject):
            this(sessionWithStreamsDBObject.session) {
        this.mStreams = sessionWithStreamsDBObject.streams.map { streamWithMeasurementsDBObject ->
            MeasurementStream(streamWithMeasurementsDBObject)
        }
    }

    constructor(sessionParams: SessionResponse): this(
        null,
        sessionParams.title,
        ArrayList(sessionParams.tag_list.split(TAGS_SEPARATOR)),
        Status.FINISHED,
        DateConverter.fromUTCString(sessionParams.start_time),
        DateConverter.fromUTCString(sessionParams.end_time),
        sessionParams.uuid,
        sessionParams.version,
        sessionParams.deleted
    ) {
        mStreams = sessionParams.streams.values.map { stream ->
            MeasurementStream(stream)
        }
    }

    enum class Status(val value: Int){
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

    fun isUploadable(): Boolean {
        // TODO: handle false if mobile && locationless
        return true
    }
}
package io.lunarlogic.aircasting.sensor

import android.location.Location
import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import java.util.*
import kotlin.collections.ArrayList

val TAGS_SEPARATOR = " "

class Session(
    val deviceId: String?,
    private val mType: Type,
    private var mName: String,
    private var mTags: ArrayList<String>,
    private var mStatus: Status,
    private val mStartTime: Date = Date(),
    private var mEndTime: Date? = null,
    val uuid: String = generateUUID(),
    var version: Int = 0,
    var deleted: Boolean = false,
    private var mStreams: List<MeasurementStream> = listOf()
) {
    constructor(sessionDBObject: SessionDBObject): this(
        sessionDBObject.deviceId,
        sessionDBObject.type,
        sessionDBObject.name,
        sessionDBObject.tags,
        sessionDBObject.status,
        sessionDBObject.startTime,
        sessionDBObject.endTime,
        sessionDBObject.uuid,
        sessionDBObject.version,
        sessionDBObject.deleted
    )

    constructor(
        deviceId: String?,
        mType: Type,
        mName: String,
        mTags: ArrayList<String>,
        mStatus: Status,
        indoor: Boolean?,
        streamingMethod: StreamingMethod?,
        location: Location?
    ): this(deviceId, mType, mName, mTags, mStatus) {
        this.mIndoor = indoor
        this.mStreamingMethod = streamingMethod
        this.mLocation = location
    }

    constructor(sessionWithStreamsDBObject: SessionWithStreamsDBObject):
            this(sessionWithStreamsDBObject.session) {
        this.mStreams = sessionWithStreamsDBObject.streams.map { streamWithMeasurementsDBObject ->
            MeasurementStream(streamWithMeasurementsDBObject)
        }
    }

    companion object {
        fun generateUUID(): String {
            return UUID.randomUUID().toString()
        }
    }

    enum class Type(val value: Int){
        MOBILE(0),
        FIXED(1)
    }

    enum class Status(val value: Int){
        NEW(-1),
        RECORDING(0),
        FINISHED(1)
    }

    enum class StreamingMethod(val value: Int) {
        CELLULAR(0),
        WIFI(1)
    }

    val type get() = mType
    val name get() = mName
    val tags get() = mTags
    val startTime get() = mStartTime
    val endTime get() = mEndTime

    private var mIndoor: Boolean? = null
    private var mStreamingMethod: StreamingMethod? = null
    private var mLocation: Location? = null

    val status get() = mStatus
    val streams get() = mStreams
    val indoor get() = mIndoor
    val streamingMethod get() = mStreamingMethod
    val location get() = mLocation

    fun startRecording() {
        mStatus = Status.RECORDING
    }

    fun stopRecording() {
        mEndTime = Date()
        mStatus = Status.FINISHED
    }

    fun isUploadable(): Boolean {
        // TODO: handle false if mobile && locationless
        return true
    }

    fun isFixed(): Boolean {
        return type == Type.FIXED
    }
}
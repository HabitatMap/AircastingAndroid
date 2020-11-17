package io.lunarlogic.aircasting.models

import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionWithStreamsDBObject
import io.lunarlogic.aircasting.sensor.microphone.MicrophoneReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

val TAGS_SEPARATOR = " "

class Session(
    val uuid: String,
    val deviceId: String?,
    private val mType: Type,
    private var mName: String,
    private var mTags: ArrayList<String>,
    private var mStatus: Status,
    private val mStartTime: Date = Date(),
    var endTime: Date? = null,
    var version: Int = 0,
    var deleted: Boolean = false,
    var followedAt: Date? = null,
    private var mStreams: List<MeasurementStream> = listOf()
) {
    constructor(sessionDBObject: SessionDBObject): this(
        sessionDBObject.uuid,
        sessionDBObject.deviceId,
        sessionDBObject.type,
        sessionDBObject.name,
        sessionDBObject.tags,
        sessionDBObject.status,
        sessionDBObject.startTime,
        sessionDBObject.endTime,
        sessionDBObject.version,
        sessionDBObject.deleted,
        sessionDBObject.followedAt
    ) {
        if (sessionDBObject.latitude != null && sessionDBObject.longitude != null) {
            this.location = Location(sessionDBObject.latitude, sessionDBObject.longitude)
        }
    }

    constructor(
        sessionUUID: String,
        deviceId: String?,
        mType: Type,
        mName: String,
        mTags: ArrayList<String>,
        mStatus: Status,
        indoor: Boolean?,
        streamingMethod: StreamingMethod?,
        location: Location?
    ): this(sessionUUID, deviceId, mType, mName, mTags, mStatus) {
        this.mIndoor = indoor
        this.mStreamingMethod = streamingMethod
        this.location = location
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
        FIXED(1);

        companion object {
            fun fromInt(value: Int) = Type.values().first { it.value == value }
        }
    }

    enum class Status(val value: Int){
        NEW(-1),
        RECORDING(0),
        FINISHED(1);

        companion object {
            fun fromInt(value: Int) = Status.values().first { it.value == value }
        }
    }

    enum class StreamingMethod(val value: Int) {
        CELLULAR(0),
        WIFI(1)
    }

    class Location(val latitude: Double, val longitude: Double) {
        companion object {
            // for indoor fixed sessions
            val INDOOR_FAKE_LOCATION = Location(200.0, 200.0)

            // if for some reason current location is not available
            val DEFAULT_LOCATION = Location(40.7128, -74.0060)

            fun get(location: android.location.Location?): Location {
                if (location == null) {
                    return DEFAULT_LOCATION
                }

                return Location(location.latitude, location.longitude)
            }
        }
    }

    private val DATE_FORMAT = "MM/dd/yy"
    private val HOUR_FORMAT = "HH:mm"

    val type get() = mType
    val name get() = mName
    val tags get() = mTags
    val startTime get() = mStartTime

    private var mIndoor: Boolean? = null
    private var mStreamingMethod: StreamingMethod? = null
    var location: Location? = null

    val status get() = mStatus
    val streams get() = mStreams
    val indoor get() = mIndoor
    val streamingMethod get() = mStreamingMethod
    val followed get() = followedAt != null

    val displayedType get() = when(type) {
        Type.MOBILE -> "mobile"
        Type.FIXED -> "fixed"
    }

    fun startRecording() {
        mStatus = Status.RECORDING
    }

    fun stopRecording() {
        endTime = Date()
        mStatus = Status.FINISHED
    }

    fun unfollow() {
        followedAt = null
    }

    fun follow() {
        followedAt = Date()
    }

    fun isUploadable(): Boolean {
        // TODO: handle false if mobile && locationless
        return true
    }

    fun isFixed(): Boolean {
        return type == Type.FIXED
    }

    fun isRecording(): Boolean {
        return status == Status.RECORDING
    }

    fun isIncomplete(): Boolean {
        return streams.isEmpty() || measurementsCount() == 0
    }

    fun hasChangedFrom(session: Session?): Boolean {
        return session?.name != name ||
                session.tags != tags ||
                session.streams.size != streams.size ||
                session.measurementsCount() != measurementsCount() ||
                session.endTime != endTime
    }

    fun streamsSortedByDetailedType(): List<MeasurementStream> {
        return streams.sortedBy { it.detailedType }
    }

    fun durationString(): String {
        val dateFormatter = dateTimeFormatter(DATE_FORMAT)
        val hourFormatter = dateTimeFormatter(HOUR_FORMAT)

        var durationString = "${dateFormatter.format(mStartTime)} ${hourFormatter.format(mStartTime)}"

        if (endTime == null) return durationString

        if (endTime!!.day != startTime.day) {
            durationString += " - ${dateFormatter.format(endTime)} ${hourFormatter.format(endTime)}"
        } else {
            durationString += "-${hourFormatter.format(endTime)}"
        }

        return durationString
    }

    fun infoString(): String {
        return "${displayedType.capitalize()}: ${sensorPackageNamesString()}"
    }

    fun sensorPackageNamesString(): String? {
        val PHONE_MIC_SENSOR_PACKAGE_NAME = "Phone Mic"
        val packageNames = mStreams.mapNotNull { s ->
            val name = s.sensorPackageName.split(":", "-").firstOrNull()
            when (name) {
                MicrophoneReader.deviceId -> PHONE_MIC_SENSOR_PACKAGE_NAME
                else -> name
            }
        }

       return packageNames.distinct().joinToString(", ")
    }

    fun measurementsCount(): Int {
        return streams.map { stream -> stream.measurements.size }.sum()
    }

    private fun dateTimeFormatter(dateTimeFormat: String): SimpleDateFormat {
        val formatter = SimpleDateFormat(dateTimeFormat, Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter
    }
}

package pl.llp.aircasting.data.model

import pl.llp.aircasting.data.api.response.search.SessionInRegionResponse
import pl.llp.aircasting.data.local.entity.*
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneDeviceItem
import java.util.*

const val TAGS_SEPARATOR = " "

open class Session(
    val uuid: String,
    val deviceId: String?,
    val deviceType: DeviceItem.Type?,
    private val mType: Type,
    private var mName: String,
    private var mTags: ArrayList<String>,
    private var mStatus: Status,
    private val mStartTime: Date = Date(),
    var endTime: Date? = null,
    var version: Int = 0,
    var deleted: Boolean = false,
    var followedAt: Date? = null,
    var contribute: Boolean = true,
    var locationless: Boolean = false,
    private var mIndoor: Boolean = false,
    private var mStreams: List<MeasurementStream> = listOf(),
    var urlLocation: String? = null,
    private var mNotes: MutableList<Note> = mutableListOf(),
    var averagingFrequency: Int = 1,
    var order: Int? = null,
    var isExternal: Boolean = false,
    var username: String? = null
) {
    constructor(sessionDBObject: SessionDBObject) : this(
        sessionDBObject.uuid,
        sessionDBObject.deviceId,
        sessionDBObject.deviceType,
        sessionDBObject.type,
        sessionDBObject.name,
        sessionDBObject.tags,
        sessionDBObject.status,
        sessionDBObject.startTime,
        sessionDBObject.endTime,
        sessionDBObject.version,
        sessionDBObject.deleted,
        sessionDBObject.followedAt,
        sessionDBObject.contribute,
        sessionDBObject.locationless,
        sessionDBObject.is_indoor,
        isExternal = sessionDBObject.isExternal == true
    ) {
        if (sessionDBObject.latitude != null && sessionDBObject.longitude != null) {
            this.location = Location(sessionDBObject.latitude, sessionDBObject.longitude)
        }
        this.urlLocation = sessionDBObject.urlLocation
    }

    constructor(
        sessionUUID: String,
        deviceId: String?,
        deviceType: DeviceItem.Type?,
        mType: Type,
        mName: String,
        mTags: ArrayList<String>,
        mStatus: Status,
        indoor: Boolean,
        streamingMethod: StreamingMethod?,
        location: Location?,
        contribute: Boolean,
        locationless: Boolean
    ) : this(sessionUUID, deviceId, deviceType, mType, mName, mTags, mStatus) {
        this.mIndoor = indoor
        this.mStreamingMethod = streamingMethod
        this.location = location
        this.contribute = contribute
        this.locationless = locationless
    }

    constructor(sessionInRegion: SessionInRegionResponse, streams: List<MeasurementStream>) : this(
        uuid = sessionInRegion.uuid,
        mName = sessionInRegion.title,
        mType = Type.FIXED,
        username = sessionInRegion.username,
        endTime = DateConverter.fromString(sessionInRegion.endTimeLocal),
        mStartTime = DateConverter.fromString(sessionInRegion.startTimeLocal) ?: Date(),
        mIndoor = sessionInRegion.isIndoor,
        deviceId = null,
        deviceType = null,
        isExternal = true,
        followedAt = Date(),
        mTags = arrayListOf(),
        mStatus = Status.RECORDING,
        mStreams = streams
    ) {
        location = Location(sessionInRegion.latitude, sessionInRegion.longitude)
    }

    constructor(sessionWithStreamsDBObject: SessionWithStreamsAndMeasurementsDBObject) :
            this(sessionWithStreamsDBObject.session) {
        this.mStreams = sessionWithStreamsDBObject.streams.map { streamWithMeasurementsDBObject ->
            MeasurementStream(streamWithMeasurementsDBObject)
        }
    }

    constructor(sessionWithStreamsDBObject: SessionWithStreamsDBObject) :
            this(sessionWithStreamsDBObject.session) {
        this.mStreams = sessionWithStreamsDBObject.streams.map { streamWithMeasurementsDBObject ->
            MeasurementStream(streamWithMeasurementsDBObject)
        }
    }

    constructor(sessionWithNotesDBObject: SessionWithNotesDBObject) :
            this(sessionWithNotesDBObject.session) {
        this.mNotes = sessionWithNotesDBObject.notes.map { noteDBObject ->
            Note(noteDBObject)
        }.toMutableList()
    }

    constructor(sessionWithStreamsAndNotesDBObject: SessionWithStreamsAndNotesDBObject) :
            this(sessionWithStreamsAndNotesDBObject.session) {
        this.mNotes = sessionWithStreamsAndNotesDBObject.notes.map { noteDBObject ->
            Note(noteDBObject)
        }.toMutableList()
        this.mStreams =
            sessionWithStreamsAndNotesDBObject.streams.map { measurementStreamDBObject ->
                MeasurementStream(measurementStreamDBObject)
            }
    }

    constructor(completeSessionDBObject: CompleteSessionDBObject) :
            this(completeSessionDBObject.session) {
        this.mNotes = completeSessionDBObject.notes.map { noteDBObject ->
            Note(noteDBObject)
        }.toMutableList()
        this.mStreams = completeSessionDBObject.streams.map { streamWithMeasurementsDBObject ->
            MeasurementStream(streamWithMeasurementsDBObject)
        }
    }

    constructor(sessionWithStreamsAndLastMeasurementsDBObject: SessionWithStreamsAndLastMeasurementsDBObject) :
            this(sessionWithStreamsAndLastMeasurementsDBObject.session) {
        this.mStreams =
            sessionWithStreamsAndLastMeasurementsDBObject.streams.map { streamWithMeasurementsDBObject ->
                MeasurementStream(streamWithMeasurementsDBObject)
            }
        this.mNotes = sessionWithStreamsAndLastMeasurementsDBObject.notes.map { noteDBObject ->
            Note(noteDBObject)
        }.toMutableList()
    }

    companion object {
        fun generateUUID(): String {
            return UUID.randomUUID().toString()
        }
    }

    enum class Type(val value: Int) {
        MOBILE(0),
        FIXED(1);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    enum class Status(val value: Int) {
        NEW(-1),
        RECORDING(0),
        FINISHED(1),
        DISCONNECTED(2);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    enum class StreamingMethod(val value: Int) {
        CELLULAR(0),
        WIFI(1)
    }

    class Location(val latitude: Double, val longitude: Double) {
        companion object {
            // for indoor fixed sessions
            val FAKE_LOCATION = Location(200.0, 200.0)

            // if for some reason current location is not available
            val DEFAULT_LOCATION = Location(40.7128, -74.0060)

            fun get(location: android.location.Location?, locationless: Boolean = false): Location {
                if (locationless) {
                    return FAKE_LOCATION
                }

                if (location == null) {
                    return DEFAULT_LOCATION
                }

                return Location(location.latitude, location.longitude)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (other == null || other !is Location) return false

            return latitude == other.latitude && longitude == other.longitude
        }

        override fun hashCode(): Int {
            var result = latitude.hashCode()
            result = 31 * result + longitude.hashCode()
            return result
        }
    }

    val type get() = mType
    var name
        get() = mName
        set(value) {
            mName = value
        }

    var tags
        get() = mTags
        set(value) {
            mTags = value
        }

    val startTime get() = mStartTime

    private var mStreamingMethod: StreamingMethod? = null
    var location: Location? = null

    val status get() = mStatus
    val streams get() = mStreams
    var notes
        get() = mNotes
        set(value) {
            mNotes = value
        }

    val indoor get() = mIndoor
    val streamingMethod get() = mStreamingMethod
    val followed get() = followedAt != null
    val activeStreams get() = mStreams.filter { stream -> !stream.deleted }

    // TODO: this was changed quite quick to add spanish translation to displayedType, maybe it should be written in cleaner way
    val displayedType
        get() = when (Locale.getDefault().language) {
            "es" -> {
                when (type) {
                    Type.MOBILE -> "móvil"
                    Type.FIXED -> "fijo"
                }
            }
            "fr" -> {
                when (type) {
                    Type.MOBILE -> "mobile"
                    Type.FIXED -> "fixé"
                }
            }
            else -> {
                when (type) {
                    Type.MOBILE -> "mobile"
                    Type.FIXED -> "fixed"
                }
            }
        }

    val tab
        get() = run {
            if (followed) SessionsTab.FOLLOWING else {
                if (isFixed()) SessionsTab.FIXED else {
                    when (status) {
                        Status.FINISHED -> SessionsTab.MOBILE_DORMANT
                        Status.RECORDING, Status.DISCONNECTED -> SessionsTab.MOBILE_ACTIVE
                        else -> SessionsTab.MOBILE_DORMANT
                    }
                }
            }
        }

    fun copy(): Session {
        return Session(
            this.uuid,
            this.deviceId,
            this.deviceType,
            this.type,
            this.name,
            this.tags,
            this.status,
            this.startTime,
            this.endTime,
            this.version,
            this.deleted,
            this.followedAt,
            this.contribute,
            this.locationless,
            this.indoor,
            this.streams,
            this.urlLocation,
            this.notes
        )
    }

    fun startRecording() {
        mStatus = Status.RECORDING
    }

    fun stopRecording(date: Date? = null) {
        endTime = date ?: Date()
        mStatus = Status.FINISHED
    }

    fun resetFollowedAtAndOrder() {
        followedAt = null
        order = null
    }

    fun setFollowedAtNow() {
        followedAt = Date()
    }

    fun isFixed(): Boolean {
        return type == Type.FIXED
    }

    fun isMobile(): Boolean {
        return type == Type.MOBILE
    }

    fun isAirBeam3(): Boolean {
        return deviceType == DeviceItem.Type.AIRBEAM3
    }

    fun isAirBeam(): Boolean {
        return deviceType == DeviceItem.Type.AIRBEAM3
                || deviceType == DeviceItem.Type.AIRBEAM2
                || deviceType == DeviceItem.Type.AIRBEAM1
    }

    fun isRecording(): Boolean {
        return status == Status.RECORDING
    }

    fun isDisconnected(): Boolean {
        return status == Status.DISCONNECTED
    }

    fun hasMeasurements(): Boolean {
        return measurementsCount() > 0
    }

    fun hasChangedFrom(session: Session?): Boolean {
        return session?.name != name ||
                session.tags != tags ||
                session.followed != followed ||
                session.streams.size != streams.size ||
                session.measurementsCount() != measurementsCount() ||
                session.status != status ||
                session.endTime != endTime ||
                session.notes.size != notes.size ||
                (session.measurementsCount() > 0 && session.lastMeasurement()?.time != lastMeasurement()?.time)
    }

    fun streamsSortedByDetailedType(): List<MeasurementStream> {
        return streams.sortedWith(compareBy({ it.sensorNameOrder() }, { it.detailedType }))
    }

    fun infoString(): String {
        return "${displayedType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}: ${sensorPackageNamesString()}"
    }

    fun sensorPackageNamesString(): String {
        val phoneMicSensorPackageName = "Phone Mic"
        val packageNames = mStreams.mapNotNull { s ->
            val name = s.sensorPackageName.split(":", "-").firstOrNull()
            when (name) {
                MicrophoneDeviceItem.DEFAULT_ID -> phoneMicSensorPackageName
                else -> name
            }
        }

        return packageNames.distinct().joinToString(", ")
    }

    private fun measurementsCount(): Int {
        return streams.sumOf { stream -> stream.measurements.size }
    }

    fun sharableLocation(): Location? {
        return if (locationless) {
            Location.FAKE_LOCATION
        } else {
            location
        }
    }

    fun lastMeasurement(): Measurement? {
        return streams.first().lastMeasurement()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Session

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}

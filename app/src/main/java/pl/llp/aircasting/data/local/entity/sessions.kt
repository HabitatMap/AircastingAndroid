package pl.llp.aircasting.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import java.util.Date

@Entity(
    tableName = "sessions",
    indices = [
        Index("device_id"),
        Index("session_order")
    ]
)
data class SessionDBObject(
    @ColumnInfo(name = "uuid") val uuid: String,
    @ColumnInfo(name = "type") val type: Session.Type,
    @ColumnInfo(name = "device_id") val deviceId: String?,
    @ColumnInfo(name = "device_type") val deviceType: DeviceItem.Type?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "tags") val tags: ArrayList<String> = arrayListOf(),
    @ColumnInfo(name = "start_time") val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date?,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?,
    @ColumnInfo(name = "status") val status: Session.Status = Session.Status.NEW,
    @ColumnInfo(name = "version") val version: Int = 0,
    @ColumnInfo(name = "deleted") val deleted: Boolean = false,
    @ColumnInfo(name = "followed_at") val followedAt: Date? = null,
    @ColumnInfo(name = "contribute") val contribute: Boolean = false,
    @ColumnInfo(name = "locationless") val locationless: Boolean = false,
    @ColumnInfo(name = "url_location") val urlLocation: String? = null,
    @ColumnInfo(name = "is_indoor") val is_indoor: Boolean = false,
    @ColumnInfo(name = "averaging_frequency") val averagingFrequency: Int = 1,
    @ColumnInfo(name = "session_order") val session_order: Int? = null,
    @ColumnInfo(name = "username") val username: String? = null,
    @ColumnInfo(name = "is_external", defaultValue = "0") val isExternal: Boolean = false,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
) {

    constructor(session: Session) :
            this(
                session.uuid,
                session.type,
                session.deviceId,
                session.deviceType,
                session.name,
                session.tags,
                session.startTime,
                session.endTime,
                session.location?.latitude,
                session.location?.longitude,
                session.status,
                session.version,
                session.deleted,
                session.followedAt,
                session.contribute,
                session.locationless,
                session.urlLocation,
                session.indoor,
                isExternal = session.isExternal
            )

    val isFixed get() = type == Session.Type.FIXED
    val isFollowed get() = followedAt != null
    val isDisconnected get() = status == Session.Status.DISCONNECTED
}

class SessionWithStreamsDBObject {
    @Embedded
    lateinit var session: SessionDBObject

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = MeasurementStreamDBObject::class
    )
    lateinit var streams: List<MeasurementStreamDBObject>
}

data class SessionWithStreamsAndMeasurementsDBObject(
    @Embedded
    val session: SessionDBObject,

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = MeasurementStreamDBObject::class
    )
    val streams: List<StreamWithMeasurementsDBObject>
) {
    val hasMeasurements get() = measurementsCount > 0
    val hasNoMeasurements get() = !hasMeasurements
    val measurementsCount get() = streams.sumOf { stream -> stream.measurements.size }
}

open class StreamWithMeasurementsDBObject(
    @Embedded
    val stream: MeasurementStreamDBObject,

    @Relation(
        parentColumn = "id",
        entityColumn = "measurement_stream_id",
        entity = MeasurementDBObject::class
    )
    val measurements: List<MeasurementDBObject>,
)

class StreamWithLastMeasurementsDBObject {
    @Embedded
    lateinit var stream: MeasurementStreamDBObject

    @Relation(
        parentColumn = "id",
        entityColumn = "stream_id",
        entity = ActiveSessionMeasurementDBObject::class
    )
    lateinit var measurements: List<ActiveSessionMeasurementDBObject>
}

class CompleteSessionDBObject {
    @Embedded
    lateinit var session: SessionDBObject

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = MeasurementStreamDBObject::class
    )
    lateinit var streams: List<StreamWithMeasurementsDBObject>

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = NoteDBObject::class
    )
    lateinit var notes: MutableList<NoteDBObject>
}

class SessionWithNotesDBObject {
    @Embedded
    lateinit var session: SessionDBObject

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = NoteDBObject::class
    )
    lateinit var notes: MutableList<NoteDBObject>
}

class SessionWithStreamsAndNotesDBObject {
    @Embedded
    lateinit var session: SessionDBObject

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = MeasurementStreamDBObject::class
    )
    lateinit var streams: List<MeasurementStreamDBObject>

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = NoteDBObject::class
    )
    lateinit var notes: MutableList<NoteDBObject>
}


class SessionWithStreamsAndLastMeasurementsDBObject {
    @Embedded
    lateinit var session: SessionDBObject

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = MeasurementStreamDBObject::class
    )
    lateinit var streams: List<StreamWithLastMeasurementsDBObject>

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = NoteDBObject::class
    )
    lateinit var notes: MutableList<NoteDBObject>
}

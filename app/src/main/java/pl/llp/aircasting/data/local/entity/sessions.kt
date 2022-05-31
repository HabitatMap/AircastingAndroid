package pl.llp.aircasting.data.local.entity

import androidx.room.*
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import java.util.*

@Entity(
    tableName = "sessions",
    indices = [
        Index("device_id"),
        Index("session_order")
    ]
)
data class SessionDBObject(
    @ColumnInfo(name = "uuid") val uuid: String,
    @ColumnInfo(name = "type") val type: LocalSession.Type,
    @ColumnInfo(name = "device_id") val deviceId: String?,
    @ColumnInfo(name = "device_type") val deviceType: DeviceItem.Type?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "tags") val tags: ArrayList<String> = arrayListOf(),
    @ColumnInfo(name = "start_time") val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date?,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?,
    @ColumnInfo(name = "status") val status: LocalSession.Status = LocalSession.Status.NEW,
    @ColumnInfo(name = "version") val version: Int = 0,
    @ColumnInfo(name = "deleted") val deleted: Boolean = false,
    @ColumnInfo(name = "followed_at") val followedAt: Date? = null,
    @ColumnInfo(name = "contribute") val contribute: Boolean = false,
    @ColumnInfo(name = "locationless") val locationless: Boolean = false,
    @ColumnInfo(name = "url_location") val urlLocation: String? = null,
    @ColumnInfo(name = "is_indoor") val is_indoor: Boolean = false,
    @ColumnInfo(name = "averaging_frequency") val averaging_frequency: Int = 1,
    @ColumnInfo(name = "session_order") val session_order: Int? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(localSession: LocalSession) :
            this(
                localSession.uuid,
                localSession.type,
                localSession.deviceId,
                localSession.deviceType,
                localSession.name,
                localSession.tags,
                localSession.startTime,
                localSession.endTime,
                localSession.location?.latitude,
                localSession.location?.longitude,
                localSession.status,
                localSession.version,
                localSession.deleted,
                localSession.followedAt,
                localSession.contribute,
                localSession.locationless,
                localSession.urlLocation,
                localSession.indoor
            )
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

class SessionWithStreamsAndMeasurementsDBObject {
    @Embedded
    lateinit var session: SessionDBObject

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = MeasurementStreamDBObject::class
    )
    lateinit var streams: List<StreamWithMeasurementsDBObject>
}

open class StreamWithMeasurementsDBObject {
    @Embedded
    lateinit var stream: MeasurementStreamDBObject

    @Relation(
        parentColumn = "id",
        entityColumn = "measurement_stream_id",
        entity = MeasurementDBObject::class
    )
    lateinit var measurements: List<MeasurementDBObject>
}

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

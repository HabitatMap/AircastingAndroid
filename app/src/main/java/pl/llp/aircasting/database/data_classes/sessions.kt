package pl.llp.aircasting.database.data_classes

import androidx.lifecycle.LiveData
import androidx.room.*
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
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
    @ColumnInfo(name = "averaging_frequency") val averaging_frequency: Int = 1,
    @ColumnInfo(name = "session_order") val session_order: Int? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(session: Session):
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
                session.indoor
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

class StreamWithMeasurementsDBObject {
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


@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions")
    fun getAll() : List<SessionDBObject>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status=:status ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithMeasurements(type: Session.Type, status: Session.Status): LiveData<List<SessionWithStreamsAndMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status IN (:statuses) ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithMeasurements(type: Session.Type, statuses: List<Int>): LiveData<List<SessionWithStreamsAndMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status IN (:statuses) ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithLastMeasurements(type: Session.Type, statuses: List<Int>): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status IN (:statuses) ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusForComplete(type: Session.Type, statuses: List<Int>): LiveData<List<CompleteSessionDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status=:status ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithNotes(type: Session.Type, status: Session.Status): LiveData<List<SessionWithStreamsAndNotesDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status=:status ORDER BY start_time DESC")
    fun loadAllByTypeAndStatus(type: Session.Type, status: Session.Status): LiveData<List<SessionWithStreamsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type ORDER BY start_time DESC")
    fun loadAllByType(type: Session.Type): LiveData<List<SessionWithStreamsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND followed_at IS NOT NULL ORDER BY followed_at DESC")
    fun loadFollowingWithMeasurements(): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type ORDER BY start_time DESC")
    fun byType(type: Session.Type): List<SessionDBObject>

    @Query("SELECT * FROM sessions WHERE status=:status")
    fun byStatus(status: Session.Status): List<SessionDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(session: SessionDBObject): Long

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadSessionAndMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadLiveDataSessionAndMeasurementsByUUID(uuid: String): LiveData<SessionWithStreamsAndMeasurementsDBObject?>

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun reloadSessionAndMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadSessionWithNotesByUUID(uuid: String):  LiveData<SessionWithStreamsAndNotesDBObject?>

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadSessionForUploadByUUID(uuid: String): CompleteSessionDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadLiveDataSessionForUploadByUUID(uuid: String): LiveData<CompleteSessionDBObject?>

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadSessionByUUID(uuid: String): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE id=:id AND deleted=0")
    fun loadSessionById(id: Long): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE status=:status AND type=:type AND deleted=0")
    fun loadSessionByStatusAndType(status: Session.Status, type: Session.Type): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE device_id=:deviceId AND status=:status AND type=:type AND deleted=0")
    fun loadSessionByDeviceIdStatusAndType(deviceId: String, status: Session.Status, type: Session.Type): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE status=:status AND type=:type AND device_type=:deviceType AND deleted=0")
    fun loadSessionByStatusTypeAndDeviceType(status: Session.Status, type: Session.Type, deviceType: DeviceItem.Type): SessionDBObject?

    @Query("UPDATE sessions SET name=:name, tags=:tags, end_time=:endTime, status=:status, version=:version, url_location=:urlLocation WHERE uuid=:uuid")
    fun update(uuid: String, name: String, tags: ArrayList<String>, endTime: Date, status: Session.Status, version: Int, urlLocation: String?)

    @Query("UPDATE sessions SET followed_at=:followedAt WHERE uuid=:uuid")
    fun updateFollowedAt(uuid: String, followedAt: Date?)

    @Query("UPDATE sessions SET session_order=:sessionOrder WHERE uuid=:uuid")
    fun updateOrder(uuid: String, sessionOrder: Int)

    @Query("UPDATE sessions SET status=:status WHERE uuid=:uuid")
    fun updateStatus(uuid: String, status: Session.Status)

    @Query("UPDATE sessions SET url_location=:urlLocation WHERE uuid=:uuid")
    fun updateUrlLocation(uuid: String, urlLocation: String?)

    @Query("UPDATE sessions SET status=:newStatus WHERE device_id=:deviceId AND status=:existingStatus")
    fun disconnectSession(newStatus: Session.Status, deviceId: String, existingStatus: Session.Status)

    @Query("UPDATE sessions SET status=:newStatus WHERE type=:type AND device_type!=:deviceType AND status=:existingStatus")
    fun disconnectSessions(newStatus: Session.Status, deviceType: DeviceItem.Type?, type: Session.Type, existingStatus: Session.Status)

    @Query("UPDATE sessions SET status=:newStatus, end_time=:endTime WHERE type=:type AND device_type=:deviceType AND status=:existingStatus")
    fun finishSessions(newStatus: Session.Status, endTime: Date, deviceType: DeviceItem.Type?, type: Session.Type, existingStatus: Session.Status)

    @Query("DELETE FROM sessions")
    fun deleteAll()

    @Query("UPDATE sessions SET deleted=1 WHERE uuid in (:uuids)")
    fun markForRemoval(uuids: List<String>)

    @Query("DELETE FROM sessions WHERE deleted=1")
    fun deleteMarkedForRemoval()

    @Query("DELETE FROM sessions WHERE uuid in (:uuids)")
    fun delete(uuids: List<String>)

    @Query("SELECT id FROM sessions WHERE type=:type AND deleted=0")
    fun loadSessionUuidsByType(type: Session.Type): List<Long>

    @Query("UPDATE sessions SET averaging_frequency=:averagingFrequency WHERE id=:sessionId")
    fun updateAveragingFrequency(sessionId: Long, averagingFrequency: Int)

    @Query("SELECT MAX(session_order) FROM sessions WHERE followed_at IS NOT NULL")
    fun getMaxSessionOrder(): Int

}

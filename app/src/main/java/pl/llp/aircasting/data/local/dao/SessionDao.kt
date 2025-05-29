package pl.llp.aircasting.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import pl.llp.aircasting.data.local.entity.CompleteSessionDBObject
import pl.llp.aircasting.data.local.entity.LatLng
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.entity.SessionWithNotesDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndLastMeasurementsDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndNotesDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import java.util.Date

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions")
    fun getAll(): List<SessionDBObject>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status IN (:statuses) ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithLastMeasurements(
        type: Session.Type,
        statuses: List<Int>
    ): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status=:status ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithNotes(
        type: Session.Type,
        status: Session.Status
    ): LiveData<List<SessionWithStreamsAndNotesDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type ORDER BY start_time DESC")
    fun loadAllByType(type: Session.Type): LiveData<List<SessionWithStreamsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND followed_at IS NOT NULL ORDER BY session_order ASC")
    fun loadFollowingWithMeasurements(): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadSessionWithNotesByUUID(uuid: String): LiveData<SessionWithStreamsAndNotesDBObject?>

    @Transaction
    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadLiveDataSessionForUploadByUUID(uuid: String): LiveData<CompleteSessionDBObject?>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type ORDER BY start_time DESC")
    suspend fun byType(type: Session.Type): List<SessionDBObject>

    @Query("SELECT * FROM sessions WHERE status=:status")
    suspend fun byStatus(status: Session.Status): List<SessionDBObject>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND followed_at IS NOT NULL")
    suspend fun loadFollowed(): List<SessionDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(session: SessionDBObject): Long

    @Transaction
    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    suspend fun loadSessionAndMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject?

    @Transaction
    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    suspend fun reloadSessionAndMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    suspend fun sessionWithNotes(uuid: String): SessionWithNotesDBObject?

    @Transaction
    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    suspend fun loadCompleteSession(uuid: String): CompleteSessionDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    suspend fun loadSessionByUUID(uuid: String): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE id=:id AND deleted=0")
    suspend fun loadSessionById(id: Long): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE device_id=:deviceId AND status=:status AND type=:type AND deleted=0")
    suspend fun loadSessionByDeviceIdStatusAndType(
        deviceId: String,
        status: Session.Status,
        type: Session.Type
    ): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE status=:status AND type=:type AND device_type=:deviceType AND deleted=0")
    suspend fun loadSessionByStatusTypeAndDeviceType(
        status: Session.Status,
        type: Session.Type,
        deviceType: DeviceItem.Type
    ): SessionDBObject?

    @Query("UPDATE sessions SET name=:name, tags=:tags, end_time=:endTime, status=:status, version=:version, url_location=:urlLocation WHERE uuid=:uuid")
    suspend fun update(
        uuid: String,
        name: String,
        tags: ArrayList<String>,
        endTime: Date?,
        status: Session.Status,
        version: Int,
        urlLocation: String?
    )

    @Query("UPDATE sessions SET followed_at=:followedAt WHERE uuid=:uuid")
    suspend fun updateFollowedAt(uuid: String, followedAt: Date?)

    @Query("UPDATE sessions SET session_order=:sessionOrder WHERE uuid=:uuid")
    suspend fun updateOrder(uuid: String, sessionOrder: Int?)

    @Query("UPDATE sessions SET status=:status WHERE uuid=:uuid")
    suspend fun updateStatus(uuid: String, status: Session.Status)

    @Query("UPDATE sessions SET url_location=:urlLocation WHERE uuid=:uuid")
    suspend fun updateUrlLocation(uuid: String, urlLocation: String?)

    @Query("UPDATE sessions SET status=:newStatus WHERE device_id=:deviceId AND status=:existingStatus")
    suspend fun disconnectSession(
        newStatus: Session.Status,
        deviceId: String,
        existingStatus: Session.Status
    )

    @Query("UPDATE sessions SET status=:newStatus WHERE type=:type AND device_type!=:deviceType AND status=:existingStatus")
    suspend fun disconnectSessions(
        newStatus: Session.Status,
        deviceType: DeviceItem.Type?,
        type: Session.Type,
        existingStatus: Session.Status
    )

    @Query("UPDATE sessions SET status=:newStatus, end_time=:endTime WHERE type=:type AND device_type=:deviceType AND status=:existingStatus")
    suspend fun finishSessions(
        newStatus: Session.Status,
        endTime: Date,
        deviceType: DeviceItem.Type?,
        type: Session.Type,
        existingStatus: Session.Status
    )

    @Query("UPDATE sessions SET deleted=1 WHERE uuid in (:uuids)")
    suspend fun markForRemoval(uuids: List<String>)

    @Query("UPDATE sessions SET deleted=1 WHERE uuid =:uuid")
    suspend fun markForRemoval(uuid: String)

    @Query("DELETE FROM sessions WHERE uuid in (:uuids)")
    suspend fun delete(uuids: List<String>)

    @Query("DELETE FROM sessions WHERE uuid =:uuid")
    suspend fun delete(uuid: String)

    @Query("SELECT id FROM sessions WHERE type=:type AND deleted=0")
    suspend fun loadSessionUuidsByType(type: Session.Type): List<Long>

    @Query("UPDATE sessions SET averaging_frequency=:averagingFrequency WHERE id=:sessionId")
    suspend fun updateAveragingFrequency(sessionId: Long, averagingFrequency: Int)

    @Query("SELECT latitude, longitude FROM sessions WHERE uuid=:uuid")
    suspend fun getLocation(uuid: String): LatLng?
}
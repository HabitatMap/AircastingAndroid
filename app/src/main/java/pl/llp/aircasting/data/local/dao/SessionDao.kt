package pl.llp.aircasting.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.llp.aircasting.data.local.entity.*
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import java.util.*

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions")
    fun getAll(): List<SessionDBObject>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status=:status ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithMeasurements(
        type: Session.Type,
        status: Session.Status
    ): LiveData<List<SessionWithStreamsAndMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status IN (:statuses) ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithMeasurements(
        type: Session.Type,
        statuses: List<Int>
    ): LiveData<List<SessionWithStreamsAndMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status IN (:statuses) ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithLastMeasurements(
        type: Session.Type,
        statuses: List<Int>
    ): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status IN (:statuses) ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusForComplete(
        type: Session.Type,
        statuses: List<Int>
    ): LiveData<List<CompleteSessionDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status=:status ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithNotes(
        type: Session.Type,
        status: Session.Status
    ): LiveData<List<SessionWithStreamsAndNotesDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status=:status ORDER BY start_time DESC")
    fun loadAllByTypeAndStatus(
        type: Session.Type,
        status: Session.Status
    ): LiveData<List<SessionWithStreamsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type ORDER BY start_time DESC")
    fun loadAllByType(type: Session.Type): LiveData<List<SessionWithStreamsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND followed_at IS NOT NULL ORDER BY session_order ASC")
    fun loadFollowingWithMeasurements(): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type ORDER BY start_time DESC")
    fun byType(type: Session.Type): List<SessionDBObject>

    @Query("SELECT * FROM sessions WHERE status=:status")
    fun byStatus(status: Session.Status): List<SessionDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(session: SessionDBObject): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSuspend(session: SessionDBObject): Long

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadSessionAndMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    suspend fun loadSessionAndMeasurementsByUUIDSuspend(uuid: String): SessionWithStreamsAndMeasurementsDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadLiveDataSessionAndMeasurementsByUUID(uuid: String): LiveData<SessionWithStreamsAndMeasurementsDBObject?>

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun reloadSessionAndMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    suspend fun reloadSessionAndMeasurementsByUUIDSuspend(uuid: String): SessionWithStreamsAndMeasurementsDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadSessionWithNotesByUUID(uuid: String): LiveData<SessionWithStreamsAndNotesDBObject?>

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
    fun loadSessionByDeviceIdStatusAndType(
        deviceId: String,
        status: Session.Status,
        type: Session.Type
    ): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE device_id=:deviceId AND status=:status AND type=:type AND deleted=0")
    suspend fun loadSessionByDeviceIdStatusAndTypeSuspend(
        deviceId: String,
        status: Session.Status,
        type: Session.Type
    ): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE status=:status AND type=:type AND device_type=:deviceType AND deleted=0")
    fun loadSessionByStatusTypeAndDeviceType(
        status: Session.Status,
        type: Session.Type,
        deviceType: DeviceItem.Type
    ): SessionDBObject?

    @Query("UPDATE sessions SET name=:name, tags=:tags, end_time=:endTime, status=:status, version=:version, url_location=:urlLocation WHERE uuid=:uuid")
    fun update(
        uuid: String,
        name: String,
        tags: ArrayList<String>,
        endTime: Date,
        status: Session.Status,
        version: Int,
        urlLocation: String?
    )

    @Query("UPDATE sessions SET followed_at=:followedAt WHERE uuid=:uuid")
    suspend fun updateFollowedAt(uuid: String, followedAt: Date?)

    @Query("UPDATE sessions SET session_order=:sessionOrder WHERE uuid=:uuid")
    suspend fun updateOrder(uuid: String, sessionOrder: Int?)

    @Query("UPDATE sessions SET status=:status WHERE uuid=:uuid")
    fun updateStatus(uuid: String, status: Session.Status)

    @Query("UPDATE sessions SET url_location=:urlLocation WHERE uuid=:uuid")
    fun updateUrlLocation(uuid: String, urlLocation: String?)

    @Query("UPDATE sessions SET status=:newStatus WHERE device_id=:deviceId AND status=:existingStatus")
    fun disconnectSession(
        newStatus: Session.Status,
        deviceId: String,
        existingStatus: Session.Status
    )

    @Query("UPDATE sessions SET status=:newStatus WHERE type=:type AND device_type!=:deviceType AND status=:existingStatus")
    fun disconnectSessions(
        newStatus: Session.Status,
        deviceType: DeviceItem.Type?,
        type: Session.Type,
        existingStatus: Session.Status
    )

    @Query("UPDATE sessions SET status=:newStatus, end_time=:endTime WHERE type=:type AND device_type=:deviceType AND status=:existingStatus")
    fun finishSessions(
        newStatus: Session.Status,
        endTime: Date,
        deviceType: DeviceItem.Type?,
        type: Session.Type,
        existingStatus: Session.Status
    )

    @Query("DELETE FROM sessions")
    fun deleteAll()

    @Query("UPDATE sessions SET deleted=1 WHERE uuid in (:uuids)")
    fun markForRemoval(uuids: List<String>)

    @Query("DELETE FROM sessions WHERE deleted=1")
    fun deleteMarkedForRemoval()

    @Query("DELETE FROM sessions WHERE uuid in (:uuids)")
    fun delete(uuids: List<String>)

    @Query("DELETE FROM sessions WHERE uuid =:uuid")
    fun delete(uuid: String)

    @Query("SELECT id FROM sessions WHERE type=:type AND deleted=0")
    fun loadSessionUuidsByType(type: Session.Type): List<Long>

    @Query("UPDATE sessions SET averaging_frequency=:averagingFrequency WHERE id=:sessionId")
    fun updateAveragingFrequency(sessionId: Long, averagingFrequency: Int)

}
package pl.llp.aircasting.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.llp.aircasting.data.local.entity.*
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import java.util.*

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions")
    fun getAll() : List<SessionDBObject>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status=:status ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithMeasurements(type: LocalSession.Type, status: LocalSession.Status): LiveData<List<SessionWithStreamsAndMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status IN (:statuses) ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithMeasurements(type: LocalSession.Type, statuses: List<Int>): LiveData<List<SessionWithStreamsAndMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status IN (:statuses) ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithLastMeasurements(type: LocalSession.Type, statuses: List<Int>): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status IN (:statuses) ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusForComplete(type: LocalSession.Type, statuses: List<Int>): LiveData<List<CompleteSessionDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status=:status ORDER BY start_time DESC")
    fun loadAllByTypeAndStatusWithNotes(type: LocalSession.Type, status: LocalSession.Status): LiveData<List<SessionWithStreamsAndNotesDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type AND status=:status ORDER BY start_time DESC")
    fun loadAllByTypeAndStatus(type: LocalSession.Type, status: LocalSession.Status): LiveData<List<SessionWithStreamsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type ORDER BY start_time DESC")
    fun loadAllByType(type: LocalSession.Type): LiveData<List<SessionWithStreamsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND followed_at IS NOT NULL ORDER BY session_order ASC")
    fun loadFollowingWithMeasurements(): LiveData<List<SessionWithStreamsAndLastMeasurementsDBObject>>

    @Query("SELECT * FROM sessions WHERE deleted=0 AND type=:type ORDER BY start_time DESC")
    fun byType(type: LocalSession.Type): List<SessionDBObject>

    @Query("SELECT * FROM sessions WHERE status=:status")
    fun byStatus(status: LocalSession.Status): List<SessionDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(session: SessionDBObject): Long

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadSessionAndMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun loadLiveDataSessionAndMeasurementsByUUID(uuid: String): LiveData<SessionWithStreamsAndMeasurementsDBObject?>

    @Query("SELECT * FROM sessions WHERE uuid=:uuid AND deleted=0")
    fun reloadSessionAndMeasurementsByUUID(uuid: String): SessionWithStreamsAndMeasurementsDBObject?

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
    fun loadSessionByStatusAndType(status: LocalSession.Status, type: LocalSession.Type): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE device_id=:deviceId AND status=:status AND type=:type AND deleted=0")
    fun loadSessionByDeviceIdStatusAndType(deviceId: String, status: LocalSession.Status, type: LocalSession.Type): SessionDBObject?

    @Query("SELECT * FROM sessions WHERE status=:status AND type=:type AND device_type=:deviceType AND deleted=0")
    fun loadSessionByStatusTypeAndDeviceType(status: LocalSession.Status, type: LocalSession.Type, deviceType: DeviceItem.Type): SessionDBObject?

    @Query("UPDATE sessions SET name=:name, tags=:tags, end_time=:endTime, status=:status, version=:version, url_location=:urlLocation WHERE uuid=:uuid")
    fun update(uuid: String, name: String, tags: ArrayList<String>, endTime: Date, status: LocalSession.Status, version: Int, urlLocation: String?)

    @Query("UPDATE sessions SET followed_at=:followedAt WHERE uuid=:uuid")
    fun updateFollowedAt(uuid: String, followedAt: Date?)

    @Query("UPDATE sessions SET session_order=:sessionOrder WHERE uuid=:uuid")
    fun updateOrder(uuid: String, sessionOrder: Int)

    @Query("UPDATE sessions SET status=:status WHERE uuid=:uuid")
    fun updateStatus(uuid: String, status: LocalSession.Status)

    @Query("UPDATE sessions SET url_location=:urlLocation WHERE uuid=:uuid")
    fun updateUrlLocation(uuid: String, urlLocation: String?)

    @Query("UPDATE sessions SET status=:newStatus WHERE device_id=:deviceId AND status=:existingStatus")
    fun disconnectSession(newStatus: LocalSession.Status, deviceId: String, existingStatus: LocalSession.Status)

    @Query("UPDATE sessions SET status=:newStatus WHERE type=:type AND device_type!=:deviceType AND status=:existingStatus")
    fun disconnectSessions(newStatus: LocalSession.Status, deviceType: DeviceItem.Type?, type: LocalSession.Type, existingStatus: LocalSession.Status)

    @Query("UPDATE sessions SET status=:newStatus, end_time=:endTime WHERE type=:type AND device_type=:deviceType AND status=:existingStatus")
    fun finishSessions(newStatus: LocalSession.Status, endTime: Date, deviceType: DeviceItem.Type?, type: LocalSession.Type, existingStatus: LocalSession.Status)

    @Query("DELETE FROM sessions")
    fun deleteAll()

    @Query("UPDATE sessions SET deleted=1 WHERE uuid in (:uuids)")
    fun markForRemoval(uuids: List<String>)

    @Query("DELETE FROM sessions WHERE deleted=1")
    fun deleteMarkedForRemoval()

    @Query("DELETE FROM sessions WHERE uuid in (:uuids)")
    fun delete(uuids: List<String>)

    @Query("SELECT id FROM sessions WHERE type=:type AND deleted=0")
    fun loadSessionUuidsByType(type: LocalSession.Type): List<Long>

    @Query("UPDATE sessions SET averaging_frequency=:averagingFrequency WHERE id=:sessionId")
    fun updateAveragingFrequency(sessionId: Long, averagingFrequency: Int)

}
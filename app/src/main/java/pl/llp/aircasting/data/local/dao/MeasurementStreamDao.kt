package pl.llp.aircasting.data.local.dao

import androidx.room.*
import pl.llp.aircasting.data.local.entity.MeasurementStreamDBObject

@Dao
interface MeasurementStreamDao {
    @Query("SELECT * FROM measurement_streams")
    fun getAll(): List<MeasurementStreamDBObject>

    @Query("SELECT * FROM measurement_streams WHERE session_id=:sessionId")
    suspend fun getSessionStreams(sessionId: Long): List<MeasurementStreamDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(measurementStream: MeasurementStreamDBObject): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSuspend(measurementStream: MeasurementStreamDBObject): Long

    @Query("SELECT * FROM measurement_streams WHERE session_id=:sessionId AND sensor_name=:sensorName")
    fun loadStreamBySessionIdAndSensorName(sessionId: Long, sensorName: String): MeasurementStreamDBObject?

    @Query("SELECT * FROM measurement_streams WHERE session_id=:sessionId AND sensor_name=:sensorName")
    suspend fun loadStreamBySessionIdAndSensorNameSuspend(sessionId: Long, sensorName: String): MeasurementStreamDBObject?

    @Query("SELECT id FROM measurement_streams WHERE session_id in (:sessionIds)")
    suspend fun getStreamsIdsBySessionIds(sessionIds: List<Long>): List<Long>

    @Query("SELECT id FROM measurement_streams WHERE session_id=:sessionId")
    suspend fun getStreamsIdsBySessionId(sessionId: Long): List<Long>

    @Delete
    suspend fun delete(streams: List<MeasurementStreamDBObject>)

    @Query("DELETE FROM measurement_streams")
    fun deleteAll()

    @Query("UPDATE measurement_streams SET deleted=1 WHERE session_id=:sessionId AND sensor_name=:sensorName")
    fun markForRemoval(sessionId: Long, sensorName: String)

    @Query("DELETE FROM measurement_streams WHERE deleted=1")
    suspend fun deleteMarkedForRemoval()
}
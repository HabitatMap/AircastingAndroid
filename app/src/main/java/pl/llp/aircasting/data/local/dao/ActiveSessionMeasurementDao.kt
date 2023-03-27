package pl.llp.aircasting.data.local.dao

import androidx.room.*
import pl.llp.aircasting.data.local.entity.ActiveSessionMeasurementDBObject
import java.util.*

@Dao
interface ActiveSessionMeasurementDao {

    @Query("SELECT count(id) FROM active_sessions_measurements WHERE session_id=:sessionId AND stream_id=:streamId")
    fun countBySessionAndStream(sessionId: Long, streamId: Long): Int

    @Query("SELECT count(id) FROM active_sessions_measurements WHERE session_id=:sessionId AND stream_id=:streamId")
    suspend fun countBySessionAndStreamSuspend(sessionId: Long, streamId: Long): Int

    @Query("SELECT id FROM active_sessions_measurements WHERE session_id=:sessionId AND stream_id=:streamId ORDER BY time ASC LIMIT 1")
    fun getOldestMeasurementId(sessionId: Long, streamId: Long): Int

    @Query("SELECT id FROM active_sessions_measurements WHERE session_id=:sessionId AND stream_id=:streamId ORDER BY time ASC LIMIT :limit")
    fun getOldestMeasurementsIds(sessionId: Long, streamId: Long, limit: Int): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: ActiveSessionMeasurementDBObject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(measurements: List<ActiveSessionMeasurementDBObject>): List<Long>

    @Query("DELETE FROM active_sessions_measurements WHERE id=:id")
    suspend fun deleteActiveSessionMeasurement(id: Int)

    @Query("DELETE FROM active_sessions_measurements WHERE session_id=:sessionId")
    suspend fun deleteActiveSessionMeasurementsBySession(sessionId: Long)

    @Query("DELETE FROM active_sessions_measurements WHERE id in (:ids)")
    suspend fun deleteActiveSessionMeasurements(ids: List<Int>)

    @Transaction
    suspend fun deleteAndInsertInTransaction(measurement: ActiveSessionMeasurementDBObject) {
        val id = getOldestMeasurementId(measurement.sessionId, measurement.streamId)
        deleteActiveSessionMeasurement(id)
        insert(measurement)
    }

    // Below transaction is not supposed to take more than 540 measurements at any time !!!
    // If it takes more then 999 measurements (because of SQLite row operation limits) the app would probably crash so we prevent it by adding "if (measurements.size > 998) -> return "
    @Transaction
    suspend fun deleteAndInsertMultipleMeasurementsInTransaction(measurements: List<ActiveSessionMeasurementDBObject>) {
        if (measurements.isEmpty() || measurements.size > 998) return
        val ids = getOldestMeasurementsIds(
            measurements.first().sessionId,
            measurements.first().streamId,
            measurements.size
        )

        deleteActiveSessionMeasurements(ids)
        insertAll(measurements)
    }
}

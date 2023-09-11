package pl.llp.aircasting.data.local.dao

import androidx.room.*
import pl.llp.aircasting.data.local.entity.LatLng
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import java.util.*

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements")
    fun getAll(): List<MeasurementDBObject>

    @Query("SELECT * FROM measurements WHERE measurement_stream_id=:streamId")
    suspend fun getByStreamId(streamId: Long): List<MeasurementDBObject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: MeasurementDBObject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(measurements: List<MeasurementDBObject>): List<Long>

    @Query("SELECT * FROM measurements WHERE session_id=:sessionId ORDER BY time DESC LIMIT 1")
    suspend fun lastForSession(sessionId: Long): MeasurementDBObject?

    @Query("SELECT time FROM measurements WHERE session_id=:sessionId AND averaging_frequency =:frequency ORDER BY time DESC LIMIT 1")
    suspend fun lastTimeOfMeasurementWithAveragingFrequency(
        sessionId: Long,
        frequency: Int
    ): Date?

    @Query("SELECT * FROM measurements WHERE session_id=:sessionId AND measurement_stream_id=:measurementStreamId ORDER BY time DESC LIMIT 1")
    suspend fun lastForStream(sessionId: Long, measurementStreamId: Long): MeasurementDBObject?

    @Query("SELECT * FROM measurements WHERE session_id=:sessionId AND measurement_stream_id=:measurementStreamId ORDER BY time")
    suspend fun getBySessionIdAndStreamId(
        sessionId: Long,
        measurementStreamId: Long
    ): List<MeasurementDBObject?>

    @Query("DELETE FROM measurements WHERE session_id=:sessionId AND time < :lastExpectedMeasurementDate ")
    suspend fun delete(sessionId: Long, lastExpectedMeasurementDate: Date)

    @Query("DELETE FROM measurements WHERE measurement_stream_id=:streamId AND id IN (:measurementsIds)")
    suspend fun deleteMeasurements(streamId: Long, measurementsIds: List<Long>)

    @Query("SELECT * FROM measurements WHERE measurement_stream_id=:streamId ORDER BY time DESC LIMIT :limit")
    suspend fun getLastMeasurements(streamId: Long, limit: Int): List<MeasurementDBObject?>

    @Query("SELECT * FROM measurements WHERE averaging_frequency < :averagingFrequency AND measurement_stream_id=:streamId ORDER BY time")
    suspend fun getMeasurementsToAverage(
        streamId: Long,
        averagingFrequency: Int
    ): List<MeasurementDBObject>

    @Transaction
    suspend fun deleteInTransaction(streamId: Long, lastExpectedMeasurementDate: Date) {
        delete(streamId, lastExpectedMeasurementDate)
    }

    @Transaction
    suspend fun deleteInTransaction(streamId: Long, measurementsIds: List<Long>) {
        deleteMeasurements(streamId, measurementsIds)
    }

    @Query("UPDATE measurements SET averaging_frequency=:averagingFrequency, value=:value, time=:time WHERE id=:measurement_id")
    suspend fun averageMeasurement(
        measurement_id: Long,
        value: Double,
        averagingFrequency: Int,
        time: Date
    )

    @Query("SELECT latitude, longitude FROM measurements WHERE latitude IS NOT NULL AND longitude IS NOT NULL AND session_id=:sessionId ORDER BY time LIMIT 1")
    suspend fun lastKnownLatLng(sessionId: Long): LatLng

}
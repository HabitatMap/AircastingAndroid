package pl.llp.aircasting.data.local.dao

import androidx.room.*
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import java.util.*

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements")
    fun getAll(): List<MeasurementDBObject>

    @Query("SELECT * FROM measurements WHERE measurement_stream_id=:streamId")
    fun getByStreamId(streamId: Long): List<MeasurementDBObject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: MeasurementDBObject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(measurements: List<MeasurementDBObject>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSuspend(measurements: List<MeasurementDBObject>): List<Long>

    @Query("SELECT * FROM measurements WHERE session_id=:sessionId ORDER BY time DESC LIMIT 1")
    fun lastForSession(sessionId: Long): MeasurementDBObject?

    @Query("SELECT * FROM measurements WHERE session_id=:sessionId ORDER BY time DESC LIMIT 1")
    suspend fun lastForSessionSuspend(sessionId: Long): MeasurementDBObject?

    @Query("SELECT * FROM measurements WHERE session_id=:sessionId AND measurement_stream_id=:measurementStreamId ORDER BY time DESC LIMIT 1")
    suspend fun lastForStream(sessionId: Long, measurementStreamId: Long): MeasurementDBObject?

    @Query("SELECT * FROM measurements WHERE session_id=:sessionId AND measurement_stream_id=:measurementStreamId ORDER BY time")
    suspend fun getBySessionIdAndStreamId(sessionId: Long, measurementStreamId: Long): List<MeasurementDBObject?>

    @Query("DELETE FROM measurements")
    fun deleteAll()

    @Query("DELETE FROM measurements WHERE session_id=:sessionId AND time < :lastExpectedMeasurementDate ")
    fun delete(sessionId: Long, lastExpectedMeasurementDate: Date)

    @Query("DELETE FROM measurements WHERE measurement_stream_id=:streamId AND id IN (:measurementsIds)")
    fun deleteMeasurements(streamId: Long, measurementsIds: List<Long>)

    @Query("SELECT * FROM measurements WHERE measurement_stream_id=:streamId ORDER BY time DESC LIMIT :limit")
    fun getLastMeasurements(streamId: Long, limit: Int): List<MeasurementDBObject?>

    @Query("SELECT AVG(value) FROM measurements WHERE measurement_stream_id=:streamId AND time >= :from AND time < :to")
    fun getAverageMeasurementFromStreamInTimeFrame(streamId: Long, from: Date, to: Date): Float

    @Query("SELECT * FROM measurements WHERE measurement_stream_id=:streamId AND time < :time ORDER BY time DESC LIMIT :limit")
    fun getLastMeasurementsForStreamStartingFromHour(streamId: Long, limit: Int, time: Date): List<MeasurementDBObject?>

    @Query("SELECT * FROM measurements WHERE measurement_stream_id=:streamId AND averaging_frequency=:averagingFrequency ORDER BY time DESC LIMIT :limit")
    fun getLastMeasurementsWithGivenAveragingFrequency(streamId: Long, limit: Int, averagingFrequency: Int): List<MeasurementDBObject?>

    @Query("SELECT * FROM measurements WHERE averaging_frequency < :averagingFrequency AND measurement_stream_id=:streamId AND time <:thresholdCrossingTime")
    fun getNonAveragedPreviousMeasurements(streamId: Long, averagingFrequency: Int, thresholdCrossingTime: Date): List<MeasurementDBObject>

    @Query("SELECT * FROM measurements WHERE averaging_frequency < :averagingFrequency AND measurement_stream_id=:streamId AND time >:thresholdCrossingTime")
    fun getNonAveragedCurrentMeasurements(streamId: Long, averagingFrequency: Int, thresholdCrossingTime: Date): List<MeasurementDBObject>

    @Query("SELECT COUNT(id) FROM measurements WHERE averaging_frequency < :newAveragingFrequency AND session_id=:sessionId AND time < :crossingThresholdTime")
    fun getNonAveragedPreviousMeasurementsCount(sessionId: Long, crossingThresholdTime: Date, newAveragingFrequency: Int): Int

    @Query("SELECT COUNT(id) FROM measurements WHERE averaging_frequency> 1 AND session_id=:sessionId AND time > :crossingThresholdTime")
    fun getNonAveragedCurrentMeasurementsCount(sessionId: Long, crossingThresholdTime: Date): Int

    @Transaction
    fun deleteInTransaction(streamId: Long, lastExpectedMeasurementDate: Date) {
        delete(streamId, lastExpectedMeasurementDate )
    }

    @Transaction
    fun deleteInTransaction(streamId: Long, measurementsIds: List<Long>) {
        deleteMeasurements(streamId, measurementsIds)
    }

    @Query("UPDATE measurements SET averaging_frequency=:averagingFrequency, value=:value, time=:time WHERE id=:measurement_id")
    fun averageMeasurement(measurement_id: Long, value: Double, averagingFrequency: Int, time: Date)
}
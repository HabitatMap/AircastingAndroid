package io.lunarlogic.aircasting.database.data_classes

import androidx.room.*
import java.util.*

@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementStreamDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("measurement_stream_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("measurement_stream_id"),
        Index("session_id")
    ]
)
data class MeasurementDBObject(
    @ColumnInfo(name = "measurement_stream_id") val measurementStreamId: Long,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "value") val value: Double,
    @ColumnInfo(name = "time") val time: Date,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?,
    @ColumnInfo(name = "is_averaged") val is_averaged: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements")
    fun getAll(): List<MeasurementDBObject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(measurement: MeasurementDBObject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(measurements: List<MeasurementDBObject>): List<Long>

    @Query("SELECT * FROM measurements WHERE session_id=:sessionId ORDER BY time DESC LIMIT 1")
    fun lastForSession(sessionId: Long): MeasurementDBObject?

    @Query("SELECT * FROM measurements WHERE session_id=:sessionId AND measurement_stream_id=:measurementStreamId ORDER BY time DESC LIMIT 1")
    fun lastForStream(sessionId: Long, measurementStreamId: Long): MeasurementDBObject?

    @Query("DELETE FROM measurements")
    fun deleteAll()

    @Query("DELETE FROM measurements WHERE session_id=:sessionId AND time < :lastExpectedMeasurementDate ")
    fun delete(sessionId: Long, lastExpectedMeasurementDate: Date)

    @Query("DELETE FROM measurements WHERE is_averaged=0 AND measurement_stream_id=:streamId AND time < :time")
    fun deleteAveraged(streamId: Long, time: Date)

    @Query("SELECT * FROM measurements WHERE measurement_stream_id=:streamId ORDER BY time DESC LIMIT :limit")
    fun getLastMeasurements(streamId: Long, limit: Int): List<MeasurementDBObject?>

    @Query("SELECT * FROM measurements WHERE is_averaged=0 AND measurement_stream_id=:streamId AND time < :time")
    fun getNonAveragedMeasurementsOlderThan(streamId: Long, time: Date): List<MeasurementDBObject>

    @Query("SELECT COUNT(id) FROM measurements WHERE is_averaged=0 AND session_id=:sessionId AND time < :time")
    fun getNonAveragedMeasurementsCount(sessionId: Long, time: Date): Int

    @Transaction
    fun deleteInTransaction(streamId: Long, lastExpectedMeasurementDate: Date) {
        delete(streamId, lastExpectedMeasurementDate )
    }

    @Transaction
    fun deleteAveragedInTransaction(streamId: Long, time: Date) {
        deleteAveraged(streamId, time)
    }

    @Query("UPDATE measurements SET is_averaged=1, value=:value WHERE id=:measurement_id")
    fun averageMeasurement(measurement_id: Long, value: Double)
}

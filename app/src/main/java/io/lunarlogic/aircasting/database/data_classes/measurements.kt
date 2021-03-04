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
    @ColumnInfo(name = "longitude") val longitude: Double?
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

    @Query("DELETE FROM measurements WHERE session_id in (:sessionIds) AND time < :lastExpectedMeasurementDate ")
    fun delete(sessionIds: List<Long>, lastExpectedMeasurementDate: Date)

    @Query("SELECT * FROM measurements WHERE session_id in (:sessionIds) ORDER BY time DESC LIMIT :limit")
    fun getLastTwentyFourHours(sessionIds: List<Long>, limit: Int): List<MeasurementDBObject?>

    @Query("SELECT * FROM measurements WHERE session_id in (:sessionIds) ORDER BY time DESC")
    fun getAllMeasurementsBySessionIds(sessionIds: List<Long>): List<MeasurementDBObject?>

    @Transaction
    fun deleteInTransaction(sessionIds: List<Long>, lastExpectedMeasurementDate: Date) {
        delete(sessionIds, lastExpectedMeasurementDate )
    }
}

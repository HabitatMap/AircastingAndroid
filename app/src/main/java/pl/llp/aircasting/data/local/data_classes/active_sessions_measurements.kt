package pl.llp.aircasting.data.local.data_classes

import androidx.room.*
import java.util.*

@Entity(
    tableName = "active_sessions_measurements",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementStreamDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("stream_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SessionDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("session_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("stream_id"),
        Index("session_id"),
        Index(value = ["session_id", "stream_id", "time"], unique = true)
    ]
)
data class ActiveSessionMeasurementDBObject(
    @ColumnInfo(name = "stream_id") val streamId: Long,
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
interface ActiveSessionMeasurementDao {

    @Query("SELECT count(id) FROM active_sessions_measurements WHERE session_id=:sessionId AND stream_id=:streamId")
    fun countBySessionAndStream(sessionId: Long, streamId: Long): Int

    @Query("SELECT id FROM active_sessions_measurements WHERE session_id=:sessionId AND stream_id=:streamId ORDER BY time ASC LIMIT 1")
    fun getOldestMeasurementId(sessionId: Long, streamId: Long): Int

    @Query("SELECT id FROM active_sessions_measurements WHERE session_id=:sessionId AND stream_id=:streamId ORDER BY time ASC LIMIT :limit")
    fun getOldestMeasurementsIds(sessionId: Long, streamId: Long, limit: Int): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(measurement: ActiveSessionMeasurementDBObject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(measurements: List<ActiveSessionMeasurementDBObject>): List<Long>

    @Query("UPDATE active_sessions_measurements SET value=:value, time=:time, latitude=:latitude, longitude=:longitude WHERE id=:id")
    fun update(id: Int, value: Double, time: Date, latitude: Double?, longitude: Double?)

    @Query("DELETE FROM active_sessions_measurements WHERE id=:id")
    fun deleteActiveSessionMeasurement(id: Int)

    @Query("DELETE FROM active_sessions_measurements WHERE session_id=:sessionId")
    fun deleteActiveSessionMeasurementsBySession(sessionId: Long)

    @Query("DELETE FROM active_sessions_measurements WHERE id in (:ids)")
    fun deleteActiveSessionMeasurements(ids: List<Int>)

    @Transaction
    fun deleteAndInsertInTransaction(measurement: ActiveSessionMeasurementDBObject) {
        val id = getOldestMeasurementId(measurement.sessionId, measurement.streamId)
        deleteActiveSessionMeasurement(id)
        insert(measurement)
    }

    // Below transaction is not supposed to take more than 540 measurements at any time !!!
    // If it takes more then 999 measurements (because of SQLite row operation limits) the app would probably crash so we prevent it by adding "if (measurements.size > 998) -> return "
    @Transaction
    fun deleteAndInsertMultipleMeasurementsInTransaction(measurements: List<ActiveSessionMeasurementDBObject>) {
        if (measurements.isEmpty() || measurements.size > 998) return
        val ids = getOldestMeasurementsIds(measurements.first().sessionId, measurements.first().streamId, measurements.size)

        deleteActiveSessionMeasurements(ids)
        insertAll(measurements)
    }
}

package io.lunarlogic.aircasting.database.data_classes

import androidx.room.*
import io.lunarlogic.aircasting.models.MeasurementStream

@Entity(
    tableName = "measurement_streams",
    foreignKeys = [
        ForeignKey(
            entity = SessionDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("session_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("session_id")
    ]
)
data class MeasurementStreamDBObject(
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "sensor_package_name") val sensorPackageName: String,
    @ColumnInfo(name = "sensor_name") val sensorName: String,
    @ColumnInfo(name = "measurement_type") val measurementType: String,
    @ColumnInfo(name = "measurement_short_type") val measurementShortType: String,
    @ColumnInfo(name = "unit_name") val unitName: String,
    @ColumnInfo(name = "unit_symbol") val unitSymbol: String,
    @ColumnInfo(name = "threshold_very_low") val thresholdVeryLow: Int,
    @ColumnInfo(name = "threshold_low") val thresholdLow: Int,
    @ColumnInfo(name = "threshold_medium") val thresholdMedium: Int,
    @ColumnInfo(name = "threshold_high") val thresholdHigh: Int,
    @ColumnInfo(name = "threshold_very_high") val thresholdVeryHigh: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(sessionId: Long, measurementStream: MeasurementStream): this(
        sessionId,
        measurementStream.sensorPackageName,
        measurementStream.sensorName,
        measurementStream.measurementType,
        measurementStream.measurementShortType,
        measurementStream.unitName,
        measurementStream.unitSymbol,
        measurementStream.thresholdVeryLow,
        measurementStream.thresholdLow,
        measurementStream.thresholdMedium,
        measurementStream.thresholdHigh,
        measurementStream.thresholdVeryHigh
    )
}

@Dao
interface MeasurementStreamDao {
    @Query("SELECT * FROM measurement_streams")
    fun getAll(): List<MeasurementStreamDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(measurementStream: MeasurementStreamDBObject): Long

    @Query("SELECT * FROM measurement_streams WHERE session_id=:sessionId AND sensor_name=:sensorName")
    fun loadStreamBySessionIdAndSensorName(sessionId: Long, sensorName: String): MeasurementStreamDBObject?

    @Query("DELETE FROM measurement_streams")
    fun deleteAll()
}

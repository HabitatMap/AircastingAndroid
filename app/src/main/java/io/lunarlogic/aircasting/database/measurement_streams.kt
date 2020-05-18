package io.lunarlogic.aircasting.database

import androidx.room.*

@Entity(tableName = "measurement_streams")
data class MeasurementStreamDBObject(
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "sensor_package_name") val sensorPackageName: String,
    @ColumnInfo(name = "sensor_name") val sensorName: String?,
    @ColumnInfo(name = "measurement_type") val measurementType: String?,
    @ColumnInfo(name = "measurement_short_type") val measurementShortType: String?,
    @ColumnInfo(name = "unit_name") val unitName: String?,
    @ColumnInfo(name = "unit_symbol") val unitSymbol: String?,
    @ColumnInfo(name = "threshold_very_low") val thresholdVeryLow: Int?,
    @ColumnInfo(name = "threshold_low") val thresholdLow: Int?,
    @ColumnInfo(name = "threshold_medium") val thresholdMedium: Int?,
    @ColumnInfo(name = "threshold_high") val thresholdHigh: Int?,
    @ColumnInfo(name = "threshold_very_high") val thresholdVeryHigh: Int?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(
    foreignKeys = arrayOf(
        ForeignKey(
            entity = SessionDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("session_id"),
            onDelete = ForeignKey.CASCADE
        )
    )
)

@Dao
interface MeasurementStreamDao {
    @Query("SELECT * FROM measurement_streams")
    fun getAll(): List<MeasurementStreamDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(measurementStream: MeasurementStreamDBObject): Long
}

package io.lunarlogic.aircasting.database.data_classes

import androidx.annotation.NonNull
import androidx.room.*
import io.lunarlogic.aircasting.sensor.MeasurementStream

@Entity(
    tableName = "sensor_thresholds",
    indices = [
        Index("sensor_name")
    ]
)
data class SensorThresholdDBObject(
    @ColumnInfo(name = "sensor_name") val sensorName: String,
    @ColumnInfo(name = "threshold_very_low") val thresholdVeryLow: Int,
    @ColumnInfo(name = "threshold_low") val thresholdLow: Int,
    @ColumnInfo(name = "threshold_medium") val thresholdMedium: Int,
    @ColumnInfo(name = "threshold_high") val thresholdHigh: Int,
    @ColumnInfo(name = "threshold_very_high") val thresholdVeryHigh: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(measurementStream: MeasurementStream): this(
        measurementStream.sensorName,
        measurementStream.thresholdVeryLow,
        measurementStream.thresholdLow,
        measurementStream.thresholdMedium,
        measurementStream.thresholdHigh,
        measurementStream.thresholdVeryHigh
    )
}

@Dao
interface SensorThresholdDao {
    @Query("SELECT * FROM sensor_thresholds WHERE sensor_name in (:sensorNames)")
    fun allBySensorNames(sensorNames: List<String>): List<SensorThresholdDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(sensorThresholdDBObject: SensorThresholdDBObject): Long

    @Query("UPDATE sensor_thresholds SET threshold_very_low=:thresholdVeryLow, threshold_low=:thresholdLow, threshold_medium=:thresholdMedium, threshold_high=:thresholdHigh, threshold_very_high=:thresholdVeryHigh WHERE sensor_name=:sensorName")
    fun update(sensorName: String, thresholdVeryLow: Int, thresholdLow: Int, thresholdMedium: Int, thresholdHigh: Int, thresholdVeryHigh: Int)

    @Query("DELETE FROM sensor_thresholds")
    fun deleteAll()
}

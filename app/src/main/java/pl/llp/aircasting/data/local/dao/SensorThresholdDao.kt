package pl.llp.aircasting.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.llp.aircasting.data.local.entity.SensorThresholdDBObject

@Dao
interface SensorThresholdDao {
    @Query("SELECT * FROM sensor_thresholds")
    fun getAll() : List<SensorThresholdDBObject>

    @Query("SELECT * FROM sensor_thresholds WHERE sensor_name in (:sensorNames)")
    suspend fun allBySensorNames(sensorNames: List<String>): List<SensorThresholdDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sensorThresholdDBObject: SensorThresholdDBObject): Long

    @Query("UPDATE sensor_thresholds SET threshold_very_low=:thresholdVeryLow, threshold_low=:thresholdLow, threshold_medium=:thresholdMedium, threshold_high=:thresholdHigh, threshold_very_high=:thresholdVeryHigh WHERE sensor_name=:sensorName")
    suspend fun update(sensorName: String, thresholdVeryLow: Int, thresholdLow: Int, thresholdMedium: Int, thresholdHigh: Int, thresholdVeryHigh: Int)
}

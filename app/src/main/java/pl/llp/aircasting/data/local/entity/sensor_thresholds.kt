package pl.llp.aircasting.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import pl.llp.aircasting.data.model.MeasurementStream

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
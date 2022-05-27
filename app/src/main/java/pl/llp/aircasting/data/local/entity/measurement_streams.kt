package pl.llp.aircasting.data.local.entity

import androidx.room.*
import pl.llp.aircasting.data.api.response.search.Sensor
import pl.llp.aircasting.data.model.MeasurementStream

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
    @ColumnInfo(name = "threshold_very_high") val thresholdVeryHigh: Int,
    @ColumnInfo(name = "deleted") var deleted: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(sessionId: Long, measurementStream: MeasurementStream) : this(
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
        measurementStream.thresholdVeryHigh,
        measurementStream.deleted
    )

    constructor(sensor: Sensor) : this(
        sensor.sessionId.toLong(),
        sensor.sensorPackageName,
        sensor.sensorName,
        sensor.measurementType,
        sensor.measurementShortType,
        sensor.unitName,
        sensor.unitSymbol,
        sensor.thresholdVeryLow,
        sensor.thresholdLow,
        sensor.thresholdMedium,
        sensor.thresholdHigh,
        sensor.thresholdVeryHigh,
        false
    )
}
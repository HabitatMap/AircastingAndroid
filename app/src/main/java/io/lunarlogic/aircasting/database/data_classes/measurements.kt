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
        Index("measurement_stream_id")
    ]
)
data class MeasurementDBObject(
    @ColumnInfo(name = "measurement_stream_id") val measurementStreamId: Long,
    @ColumnInfo(name = "value") val value: Double,
    @ColumnInfo(name = "time") val time: Date
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements")
    fun getAll(): List<MeasurementDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(measurement: MeasurementDBObject): Long
}

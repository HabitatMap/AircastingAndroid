package io.lunarlogic.aircasting.database

import androidx.room.*
import java.util.*

@Entity(tableName = "measurements")
data class MeasurementDBObject(
    @ColumnInfo(name = "measurement_stream_id") val measurementStreamId: Long,
    @ColumnInfo(name = "value") val value: Double?,
    @ColumnInfo(name = "time") val time: Date?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(
    foreignKeys = arrayOf(
        ForeignKey(
            entity = MeasurementStreamDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("measurement_stream_id"),
            onDelete = ForeignKey.CASCADE
        )
    )
)

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements")
    fun getAll(): List<MeasurementDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(measurement: MeasurementDBObject): Long
}

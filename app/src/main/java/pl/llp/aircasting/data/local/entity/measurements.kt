package pl.llp.aircasting.data.local.entity

import androidx.room.*
import pl.llp.aircasting.data.local.entity.MeasurementStreamDBObject
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
        Index("session_id"),
//        Index(value = ["session_id", "measurement_stream_id", "time"], unique = true)
    ]
)
data class MeasurementDBObject(
    @ColumnInfo(name = "measurement_stream_id") val measurementStreamId: Long,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "value") val value: Double,
    @ColumnInfo(name = "time") val time: Date,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?,
    @ColumnInfo(name = "averaging_frequency") val averaging_frequency: Int = 1
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

package pl.llp.aircasting.data.local.entity

import androidx.room.*
import pl.llp.aircasting.util.helpers.services.AverageableMeasurement
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
        Index("session_id")
    ]
)
data class MeasurementDBObject(
    @ColumnInfo(name = "measurement_stream_id") val measurementStreamId: Long,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "value") override var value: Double,
    @ColumnInfo(name = "time") override var time: Date,
    @ColumnInfo(name = "latitude") override var latitude: Double?,
    @ColumnInfo(name = "longitude") override var longitude: Double?,
    @ColumnInfo(name = "averaging_frequency") var averagingFrequency: Int = 1
) : AverageableMeasurement {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

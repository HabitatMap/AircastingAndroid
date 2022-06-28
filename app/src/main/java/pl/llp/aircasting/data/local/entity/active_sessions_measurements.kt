package pl.llp.aircasting.data.local.entity

import androidx.room.*
import java.util.*

@Entity(
    tableName = "active_sessions_measurements",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementStreamDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("stream_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SessionDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("session_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("stream_id"),
        Index("session_id"),
        Index(value = ["session_id", "stream_id", "time"], unique = true)
    ]
)
data class ActiveSessionMeasurementDBObject(
    @ColumnInfo(name = "stream_id") val streamId: Long,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "value") val value: Double,
    @ColumnInfo(name = "time") val time: Date,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
package pl.llp.aircasting.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ext_sessions",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementStreamDBObject::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("stream_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExtSessionsDBObject(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "uuid") val uuid: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "end_time_local") val endTimeLocal: String,
    @ColumnInfo(name = "start_time_local") val startTimeLocal: String,
    @ColumnInfo(name = "last_hour_average") val lastHourAverage: Double,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: String,
    @ColumnInfo(name = "is_indoor") val isIndoor: Boolean,
    @ColumnInfo(name = "stream_id") val streamId: Int
)
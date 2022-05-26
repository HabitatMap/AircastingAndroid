package pl.llp.aircasting.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.llp.aircasting.data.api.response.search.Streams

@Entity(tableName = "ext_session")
data class ExtSession(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "uuid") val uuid: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "endTimeLocal") val endTimeLocal: String,
    @ColumnInfo(name = "startTimeLocal") val startTimeLocal: String,
    @ColumnInfo(name = "lastHourAverage") val lastHourAverage: Double,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: String,
    @ColumnInfo(name = "isIndoor") val isIndoor: Boolean,
    @ColumnInfo(name = "streams") val streams: Streams,
)
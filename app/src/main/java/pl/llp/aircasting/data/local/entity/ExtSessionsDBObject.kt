package pl.llp.aircasting.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.llp.aircasting.data.api.response.search.Session
import java.util.*

@Entity(
    tableName = "ext_sessions"
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
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "is_indoor") val isIndoor: Boolean,
    @ColumnInfo(name = "followed_at") val followedAt: Date? = null
) {
    constructor(apiSession: Session) : this(
        id = apiSession.id,
        uuid = apiSession.uuid,
        title = apiSession.title,
        type = apiSession.type,
        username = apiSession.username,
        endTimeLocal = apiSession.endTimeLocal,
        startTimeLocal = apiSession.startTimeLocal,
        lastHourAverage = apiSession.lastHourAverage,
        latitude = apiSession.latitude,
        longitude = apiSession.longitude,
        isIndoor = apiSession.isIndoor,
    )
}
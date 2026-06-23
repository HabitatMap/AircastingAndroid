package pl.llp.aircasting.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_locations")
data class TrackedLocationDBObject(
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "time") val time: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

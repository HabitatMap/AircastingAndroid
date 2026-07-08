package pl.llp.aircasting.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.llp.aircasting.data.local.entity.TrackedLocationDBObject

@Dao
interface TrackedLocationDao {
    // Ordered by time so the in-memory list stays ascending — AirBeamMiniV2StateRepository
    // .getClosestLocation() binary-searches it and relies on that ordering.
    @Query("SELECT * FROM tracked_locations ORDER BY time ASC")
    suspend fun getAll(): List<TrackedLocationDBObject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trackedLocation: TrackedLocationDBObject): Long

    @Query("DELETE FROM tracked_locations")
    suspend fun deleteAll()
}

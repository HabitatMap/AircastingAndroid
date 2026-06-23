package pl.llp.aircasting.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.llp.aircasting.data.local.entity.TrackedLocationDBObject

@Dao
interface TrackedLocationDao {
    @Query("SELECT * FROM tracked_locations")
    suspend fun getAll(): List<TrackedLocationDBObject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trackedLocation: TrackedLocationDBObject): Long

    @Query("DELETE FROM tracked_locations")
    suspend fun deleteAll()
}

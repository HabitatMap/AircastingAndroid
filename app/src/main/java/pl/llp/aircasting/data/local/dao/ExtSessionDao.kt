package pl.llp.aircasting.data.local.dao

import androidx.room.*
import pl.llp.aircasting.data.local.entity.ExtSessionsDBObject

@Dao
interface ExtSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(extSession: ExtSessionsDBObject)

    @Delete
    suspend fun delete(extSession: ExtSessionsDBObject)

    @Query("Select * from ext_sessions order by id ASC")
    suspend fun getAllFollowedSessions(): List<ExtSessionsDBObject>
}
package pl.llp.aircasting.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pl.llp.aircasting.data.local.entity.ExtSessionsDBObject

@Dao
interface ExtSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(extSession: ExtSessionsDBObject)

    @Delete
    fun delete(extSession: ExtSessionsDBObject)

    @Query("Select * from ext_sessions order by id ASC")
    fun getAllFollowedSessions(): LiveData<List<ExtSessionsDBObject>>
}
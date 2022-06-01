package pl.llp.aircasting.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pl.llp.aircasting.data.local.entity.ExtSessionsDBObject
import pl.llp.aircasting.data.local.entity.ExternalSessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.local.entity.ExternalSessionWithStreamsDBObject

@Dao
interface ExtSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(extSession: ExtSessionsDBObject)

    @Delete
    fun delete(extSession: ExtSessionsDBObject)

    @Query("Select * from ext_sessions order by id ASC")
    fun getAllFollowedSessions(): LiveData<List<ExtSessionsDBObject>>

    @Query("Select * from ext_sessions order by id ASC")
    fun getAllFollowedSessionsWithStreams(): LiveData<List<ExternalSessionWithStreamsDBObject>>

    @Query("Select * from ext_sessions order by id ASC")
    fun getAllFollowedSessionsWithStreamsAndMeasurements(): LiveData<List<ExternalSessionWithStreamsAndMeasurementsDBObject>>
}
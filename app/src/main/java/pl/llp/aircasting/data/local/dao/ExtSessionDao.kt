package pl.llp.aircasting.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import pl.llp.aircasting.data.local.entity.ExtSessionsDBObject

@Dao
interface ExtSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(extSession: ExtSessionsDBObject)

    @Delete
    fun delete(extSession: ExtSessionsDBObject)
}
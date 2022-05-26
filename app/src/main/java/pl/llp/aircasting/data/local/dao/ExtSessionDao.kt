package pl.llp.aircasting.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import pl.llp.aircasting.data.local.entity.ExtSession

@Dao
interface ExtSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(extSession: ExtSession)

    @Delete
    fun delete(extSession: ExtSession)
}
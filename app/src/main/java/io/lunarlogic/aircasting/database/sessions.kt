package io.lunarlogic.aircasting.database

import androidx.room.*
import java.util.*

@Entity(tableName = "sessions")
data class Session(
    @ColumnInfo(name = "uuid") val uuid: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "tags") val tags: List<String>?,
    @ColumnInfo(name = "start_time") val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions")
    fun getAll(): List<Session>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(session: Session): Long
}

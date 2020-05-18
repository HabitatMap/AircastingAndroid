package io.lunarlogic.aircasting.database

import androidx.room.*
import io.lunarlogic.aircasting.sensor.Session
import java.util.*

@Entity(tableName = "sessions")
data class SessionDBObject(
    @ColumnInfo(name = "uuid") val uuid: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "tags") val tags: List<String>?,
    @ColumnInfo(name = "start_time") val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date?,
    @ColumnInfo(name = "status") val status: Int = Session.Status.NEW.value
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    constructor(session: Session):
            this(session.uuid.toString(), session.name, session.tags, session.startTime, session.endTime, session.status.value)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions")
    fun getAll(): List<SessionDBObject>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(session: SessionDBObject): Long
}

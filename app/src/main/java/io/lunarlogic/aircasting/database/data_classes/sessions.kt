package io.lunarlogic.aircasting.database.data_classes

import androidx.lifecycle.LiveData
import androidx.room.*
import io.lunarlogic.aircasting.sensor.Session
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "sessions")
data class SessionDBObject(
    @ColumnInfo(name = "uuid") val uuid: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "tags") val tags: ArrayList<String> = arrayListOf(),
    @ColumnInfo(name = "start_time") val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date?,
    @ColumnInfo(name = "status") val status: Session.Status = Session.Status.NEW
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(session: Session):
            this(
                session.uuid,
                session.name,
                session.tags,
                session.startTime,
                session.endTime,
                session.status
            )
}


class SessionWithStreamsDBObject {
    @Embedded
    lateinit var session: SessionDBObject

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = MeasurementStreamDBObject::class
    )
    lateinit var streams: List<StreamWithMeasurementsDBObject>
}


class StreamWithMeasurementsDBObject {
    @Embedded
    lateinit var stream: MeasurementStreamDBObject

    @Relation(
        parentColumn = "id",
        entityColumn = "measurement_stream_id",
        entity = MeasurementDBObject::class
    )
    lateinit var measurements: List<MeasurementDBObject>
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY start_time DESC")
    fun loadAllWithMeasurements(): LiveData<List<SessionWithStreamsDBObject>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(session: SessionDBObject): Long

    @Query("SELECT * FROM sessions WHERE uuid=:uuid")
    fun loadSessionAndMeasurementsByUUID(uuid: String): SessionWithStreamsDBObject?

    @Query("SELECT * FROM sessions WHERE uuid=:uuid")
    fun loadSessionByByUUID(uuid: String): SessionDBObject?

    @Query("UPDATE sessions SET name=:name, tags=:tags, end_time=:endTime, status=:status WHERE uuid=:uuid")
    fun update(uuid: String, name: String, tags: ArrayList<String>, endTime: Date, status: Session.Status)

    @Query("DELETE FROM sessions")
    fun delete_all()
}

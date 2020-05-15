package io.lunarlogic.aircasting.database

import androidx.room.*
import java.util.*

@Entity
data class Measurement(
    @ColumnInfo(name = "value") val value: Double?,
    @ColumnInfo(name = "time") val time: Date?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurement")
    fun getAll(): List<Measurement>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(measurement: Measurement): Long
}

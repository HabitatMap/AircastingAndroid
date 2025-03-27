package pl.llp.aircasting.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.llp.aircasting.data.local.dao.ActiveSessionMeasurementDao
import pl.llp.aircasting.data.local.dao.MeasurementDao
import pl.llp.aircasting.data.local.dao.MeasurementStreamDao
import pl.llp.aircasting.data.local.dao.NoteDao
import pl.llp.aircasting.data.local.dao.SensorThresholdDao
import pl.llp.aircasting.data.local.dao.SessionDao
import pl.llp.aircasting.data.local.entity.ActiveSessionMeasurementDBObject
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.local.entity.MeasurementStreamDBObject
import pl.llp.aircasting.data.local.entity.NoteDBObject
import pl.llp.aircasting.data.local.entity.SensorThresholdDBObject
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.util.converters.DateConverter
import pl.llp.aircasting.util.converters.DeviceTypeConverter
import pl.llp.aircasting.util.converters.SessionStatusConverter
import pl.llp.aircasting.util.converters.SessionTypeConverter
import pl.llp.aircasting.util.converters.TagsConverter

@Database(
    entities = [SessionDBObject::class,
        MeasurementStreamDBObject::class,
        MeasurementDBObject::class,
        SensorThresholdDBObject::class,
        NoteDBObject::class,
        ActiveSessionMeasurementDBObject::class],
    version = 34,
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    TagsConverter::class,
    SessionStatusConverter::class,
    SessionTypeConverter::class,
    DeviceTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessions(): SessionDao
    abstract fun measurementStreams(): MeasurementStreamDao
    abstract fun measurements(): MeasurementDao
    abstract fun sensorThresholds(): SensorThresholdDao
    abstract fun notes(): NoteDao
    abstract fun activeSessionsMeasurements(): ActiveSessionMeasurementDao
}

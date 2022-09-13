package pl.llp.aircasting.data.local

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.*
import pl.llp.aircasting.data.local.dao.*
import pl.llp.aircasting.data.local.entity.*
import pl.llp.aircasting.data.local.migrations.MIGRATION_16_17
import pl.llp.aircasting.util.converters.*

@Database(
    version = 32,
    entities = [SessionDBObject::class,
        MeasurementStreamDBObject::class,
        MeasurementDBObject::class,
        SensorThresholdDBObject::class,
        NoteDBObject::class,
        ActiveSessionMeasurementDBObject::class],
    autoMigrations = [AutoMigration(from = 16, to = 32)]
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

class DatabaseProvider {
    companion object {
        private val DB_NAME = "aircasting"

        @SuppressLint("StaticFieldLeak")
        private lateinit var mContext: Context
        var mAppDatabase: AppDatabase? = null

        fun setup(context: Context) {
            mContext = context.applicationContext
        }

        fun get(): AppDatabase {
            if (mAppDatabase == null) {
                mAppDatabase = Room.databaseBuilder(
                    mContext,
                    AppDatabase::class.java, DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_16_17)
                    .build()
            }

            return mAppDatabase!!
        }
    }
}

package pl.llp.aircasting.data.local

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.*
import pl.llp.aircasting.data.local.converters.*
import pl.llp.aircasting.data.local.data_classes.*
import pl.llp.aircasting.data.local.migrations.*


@Database(
    entities = arrayOf(
        SessionDBObject::class,
        MeasurementStreamDBObject::class,
        MeasurementDBObject::class,
        SensorThresholdDBObject::class,
        NoteDBObject::class,
        ActiveSessionMeasurementDBObject::class
    ),
    version = 30,
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
                    .addMigrations(
                        MIGRATION_16_17,
                        MIGRATION_17_18,
                        MIGRATION_18_19,
                        MIGRATION_19_20,
                        MIGRATION_20_21,
                        MIGRATION_21_22,
                        MIGRATION_22_23,
                        MIGRATION_23_24,
                        MIGRATION_24_25,
                        MIGRATION_25_26,
                        MIGRATION_26_27,
                        MIGRATION_27_28,
                        MIGRATION_28_29
                    )
                    .build()
            }

            return mAppDatabase!!
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun runQuery(block: (scope: CoroutineScope) -> Unit) {
            GlobalScope.launch(Dispatchers.IO) {
                block(this)
            }
        }

        fun backToUIThread(scope: CoroutineScope, uiBlock: () -> Unit) {
            scope.launch(Dispatchers.Main) {
                uiBlock()
            }
        }
    }
}
package io.lunarlogic.aircasting.database

import android.content.Context
import androidx.room.*
import io.lunarlogic.aircasting.database.converters.*
import io.lunarlogic.aircasting.database.data_classes.*
import io.lunarlogic.aircasting.database.migrations.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Database(
    entities = arrayOf(
        SessionDBObject::class,
        MeasurementStreamDBObject::class,
        MeasurementDBObject::class,
        SensorThresholdDBObject::class,
        NoteDBObject::class
    ),
    version = 26,
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
}

class DatabaseProvider {
    companion object {
        private val DB_NAME = "aircasting"

        private lateinit var mContext: Context
        var mAppDatabase: AppDatabase? = null

        fun setup(context: Context) {
            mContext = context
        }

        fun get(): AppDatabase {
            if (mAppDatabase == null) {
                mAppDatabase = Room.databaseBuilder(
                    mContext,
                    AppDatabase::class.java, DB_NAME
                ).addMigrations(
                    MIGRATION_16_17,
                    MIGRATION_17_18,
                    MIGRATION_18_19,
                    MIGRATION_19_20,
                    MIGRATION_20_21,
                    MIGRATION_21_22,
                    MIGRATION_22_23,
                    MIGRATION_23_24,
                    MIGRATION_24_25,
                    MIGRATION_25_26
                ).build()
            }

            return mAppDatabase!!
        }

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

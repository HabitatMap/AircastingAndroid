package io.lunarlogic.aircasting.database

import android.content.Context
import androidx.room.*
import io.lunarlogic.aircasting.database.converters.*
import io.lunarlogic.aircasting.database.data_classes.*
import io.lunarlogic.aircasting.database.migrations.*
import io.lunarlogic.aircasting.database.migrations.MIGRATION_16_17
import io.lunarlogic.aircasting.database.migrations.MIGRATION_17_18
import io.lunarlogic.aircasting.database.migrations.MIGRATION_18_19
import io.lunarlogic.aircasting.database.migrations.MIGRATION_19_20
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Database(
    entities = arrayOf(
        SessionDBObject::class,
        MeasurementStreamDBObject::class,
        MeasurementDBObject::class,
        SensorThresholdDBObject::class
    ),
    version = 21,
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
                    MIGRATION_20_21

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

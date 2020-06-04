package io.lunarlogic.aircasting.database

import android.content.Context
import androidx.room.*
import io.lunarlogic.aircasting.database.converters.DateConverter
import io.lunarlogic.aircasting.database.converters.SessionStatusConverter
import io.lunarlogic.aircasting.database.converters.TagsConverter
import io.lunarlogic.aircasting.database.data_classes.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Database(
    entities = arrayOf(
        SessionDBObject::class,
        MeasurementStreamDBObject::class,
        MeasurementDBObject::class
    ),
    version = 12
)
@TypeConverters(
    DateConverter::class,
    TagsConverter::class,
    SessionStatusConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessions(): SessionDao
    abstract fun measurementStreams(): MeasurementStreamDao
    abstract fun measurements(): MeasurementDao
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
                ).fallbackToDestructiveMigration().build()
            }

            return mAppDatabase!!
        }

        fun runQuery(block: () -> Unit) {
            GlobalScope.launch(Dispatchers.IO) {
                block()
            }
        }
    }
}
package io.lunarlogic.aircasting.database

import android.content.Context
import androidx.room.*
import io.lunarlogic.aircasting.database.converters.DateConverter
import io.lunarlogic.aircasting.database.converters.TagsConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Database(
    entities = arrayOf(
        SessionDBObject::class,
        MeasurementStreamDBObject::class,
        MeasurementDBObject::class
    ),
    version = 5
)
@TypeConverters(DateConverter::class, TagsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessions(): SessionDao
    abstract fun measurementStreams(): MeasurementStreamDao
    abstract fun measurements(): MeasurementDao
}

class DatabaseProvider {
    companion object {
        private val DB_NAME = "aircasting"

        var mAppDatabase: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            if (mAppDatabase == null) {
                mAppDatabase = Room.databaseBuilder(
                    context,
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
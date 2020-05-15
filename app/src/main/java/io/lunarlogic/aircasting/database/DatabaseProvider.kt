package io.lunarlogic.aircasting.database

import android.content.Context
import androidx.room.*


@Database(
    entities = arrayOf(
        Session::class,
        MeasurementStream::class,
        Measurement::class
    ),
    version = 4
)
@TypeConverters(DateConverter::class, TagsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessions(): SessionDao
    abstract fun measurementStreams(): MeasurementStreamDao
    abstract fun measurements(): MeasurementDao
}

class DatabaseProvider {
    private val DB_NAME = "aircasting"

    var mAppDatabase: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        if (mAppDatabase == null) {
            mAppDatabase = Room.databaseBuilder(
                context,
                AppDatabase::class.java, DB_NAME
            ).build()
        }

        return mAppDatabase!!
    }
}
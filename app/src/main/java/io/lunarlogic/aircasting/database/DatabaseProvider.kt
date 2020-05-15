package io.lunarlogic.aircasting.database

import android.content.Context
import androidx.room.*


@Database(entities = arrayOf(Measurement::class), version = 3)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao
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
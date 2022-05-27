package pl.llp.aircasting.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `ext_sessions` (`id` INTEGER NOT NULL, `uuid` TEXT NOT NULL," +
                    " `title` TEXT NOT NULL, `type` TEXT NOT NULL, `username` TEXT NOT NULL, `endTimeLocal` TEXT NOT NULL, " +
                    "`startTimeLocal` TEXT NOT NULL, `lastHourAverage` REAL NOT NULL," +
                    " `latitude` REAL NOT NULL, `longitude` TEXT NOT NULL, `isIndoor` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
    }
}
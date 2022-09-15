package pl.llp.aircasting.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_31_32 = object : Migration(31, 32) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE `notes` (`photo_location` TEXT, " + "PRIMARY KEY(`id`))"
        )
    }
}
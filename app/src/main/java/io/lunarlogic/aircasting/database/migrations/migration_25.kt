package io.lunarlogic.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_24_25 = object: Migration(24, 25) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `sessions` ADD `notes` TEXT NOT NULL")
        database.execSQL(
            "CREATE TABLE `notes` (" +
                "`note_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`session_id` INTEGER NOT NULL, " +
                "`text` TEXT NOT NULL, " +
                "`date` INTEGER NOT NULL, " +
                "`latitude` REAL, " +
                "`longtitude` REAL, " +
                    "FOREIGN KEY (`session_id`) REFERENCES `sessions`(`id`))" )
    }

}

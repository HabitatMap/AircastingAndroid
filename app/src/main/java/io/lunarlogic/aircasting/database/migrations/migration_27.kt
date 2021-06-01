package io.lunarlogic.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `active_sessions_measurements` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`session_id` INTEGER NOT NULL, " +
                "`stream_id` INTEGER NOT NULL, " +
                "`value` REAL NOT NULL, " +
                "`time` INTEGER NOT NULL, " +
                "`latitude` REAL, " +
                "`longitude` REAL, " +
                "FOREIGN KEY(`stream_id`) REFERENCES `measurement_streams`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE " +
                "FOREIGN KEY(`session_id`) REFERENCES `sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE " +
                ")"
        )
        database.execSQL("CREATE  INDEX `index_active_sessions_measurements_stream_id` ON `active_sessions_measurements` (`stream_id`)")
        database.execSQL("CREATE  INDEX `index_active_sessions_measurements_sessions_id` ON `active_sessions_measurements` (`session_id`)")
    }
}

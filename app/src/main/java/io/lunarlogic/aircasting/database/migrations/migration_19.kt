package io.lunarlogic.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // It's not possible to rename column in sqlite, so we need to
        // drop whole table and create it back again with the new field
        database.execSQL("DROP TABLE `sessions`")
        database.execSQL("DELETE FROM `measurements`")
        database.execSQL("DELETE FROM `measurement_streams`")
        database.execSQL("CREATE TABLE `sessions` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`uuid` TEXT NOT NULL, " +
                "`type` INTEGER NOT NULL, " +
                "`device_id` TEXT, " +
                "`name` TEXT NOT NULL, " +
                "`tags` TEXT NOT NULL, " +
                "`start_time` INTEGER NOT NULL, " +
                "`end_time` INTEGER, " +
                "`latitude` REAL, " +
                "`longitude` REAL, " +
                "`status` INTEGER NOT NULL, " +
                "`version` INTEGER NOT NULL, " +
                "`deleted` INTEGER NOT NULL, " +
                "`followed_at` INTEGER NULL" +
                ")"
        )
        database.execSQL("CREATE  INDEX `index_sessions_device_id` ON `sessions` (`device_id`)")
    }
}

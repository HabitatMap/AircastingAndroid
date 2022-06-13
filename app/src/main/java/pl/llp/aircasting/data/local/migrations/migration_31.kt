package pl.llp.aircasting.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // TODO: Update the migration
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `ext_sessions` (`id` INTEGER NOT NULL, `uuid` TEXT NOT NULL, `title` TEXT NOT NULL, `type` TEXT NOT NULL, `username` TEXT NOT NULL, `end_time_local` TEXT NOT NULL, `start_time_local` TEXT NOT NULL, `last_hour_average` REAL NOT NULL, `latitude` REAL NOT NULL, `longitude` TEXT NOT NULL, `is_indoor` INTEGER NOT NULL, `stream_id` INTEGER NOT NULL, `followed_at` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`stream_id`) REFERENCES `measurement_streams`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
    }
}
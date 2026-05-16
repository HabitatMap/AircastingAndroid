package pl.llp.aircasting.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_35_36 = object : Migration(35, 36) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "DELETE FROM `measurements` WHERE `id` NOT IN (" +
                    "SELECT MIN(`id`) FROM `measurements` " +
                    "GROUP BY `session_id`, `measurement_stream_id`, `time`)"
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS " +
                    "`index_measurements_session_id_measurement_stream_id_time` ON `measurements` " +
                    "(`session_id`, `measurement_stream_id`, `time`)"
        )
    }
}

package pl.llp.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_28_29 = object : Migration(28, 29) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE UNIQUE INDEX " +
                    "`unique_index_measurements_duplicates` ON `measurements` " +
                    "(`session_id`, `stream_id`, `time`)"
        )
        database.execSQL(
            "CREATE UNIQUE INDEX " +
                    "`unique_index_measurements_duplicates` ON `active_sessions_measurements` " +
                    "(`session_id`, `stream_id`, `time`)"
        )
    }
}

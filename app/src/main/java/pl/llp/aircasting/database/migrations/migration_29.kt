package pl.llp.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_28_29 = object : Migration(28, 29) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "DELETE FROM `measurements`"
        )

        database.execSQL(
            "CREATE UNIQUE INDEX " +
                    "`index_measurements_session_id_measurement_stream_id_time` ON `measurements` " +
                    "(`session_id`, `measurement_stream_id`, `time`)"
        )
        database.execSQL(
            "CREATE UNIQUE INDEX " +
                    "`index_active_sessions_measurements_session_id_stream_id_time` ON `active_sessions_measurements` " +
                    "(`session_id`, `stream_id`, `time`)"
        )
    }
}
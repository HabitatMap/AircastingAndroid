package pl.llp.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_27_28 = object : Migration(27, 28) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `active_sessions_measurements` ADD `averaging_frequency` INTEGER NOT NULL DEFAULT 1")
    }
}

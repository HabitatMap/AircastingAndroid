package pl.llp.aircasting.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE `sessions` ADD `is_external` INTEGER DEFAULT 0"
        )
        database.execSQL(
            "ALTER TABLE `sessions` ADD `username` TEXT"
        )
    }
}
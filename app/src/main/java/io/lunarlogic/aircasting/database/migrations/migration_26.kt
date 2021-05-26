package io.lunarlogic.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `measurements` ADD `averaging_frequency` INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE `sessions` ADD `averaging_frequency` INTEGER NOT NULL DEFAULT 1")
    }
}

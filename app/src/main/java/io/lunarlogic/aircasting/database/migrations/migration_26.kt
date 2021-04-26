package io.lunarlogic.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_25_26 = object: Migration(25,26) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `notes` ADD `photo_location` TEXT DEFAULT NULL")
    }
}

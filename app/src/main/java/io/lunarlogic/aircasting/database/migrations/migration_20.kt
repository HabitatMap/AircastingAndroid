package io.lunarlogic.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_19_20 = object : Migration(19,20) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `sessions` ADD `url_location` TEXT NULL")
    }
}

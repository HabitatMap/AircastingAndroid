package io.lunarlogic.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `sessions` ADD `latitude` REAL NULL")
        database.execSQL("ALTER TABLE `sessions` ADD `longitude` REAL NULL")
    }
}

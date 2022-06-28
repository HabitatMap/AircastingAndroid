package pl.llp.aircasting.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `sessions` ADD `contribute` INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE `sessions` ADD `locationless` INTEGER NOT NULL DEFAULT 0")
    }
}

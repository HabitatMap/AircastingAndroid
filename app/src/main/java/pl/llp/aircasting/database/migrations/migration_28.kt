package pl.llp.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_27_28 = object : Migration(27, 28) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `sessions` ADD `session_order` INTEGER DEFAULT NULL")
        database.execSQL("CREATE  INDEX `index_sessions_sessions_order` ON `sessions` (`session_order`)")
    }
}

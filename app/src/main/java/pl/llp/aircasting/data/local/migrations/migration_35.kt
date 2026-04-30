package pl.llp.aircasting.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE sessions ADD COLUMN session_token TEXT")
        database.execSQL("ALTER TABLE sessions ADD COLUMN fixed_pm1_index INTEGER")
        database.execSQL("ALTER TABLE sessions ADD COLUMN fixed_pm25_index INTEGER")
    }
}

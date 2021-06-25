package pl.llp.aircasting.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `sensor_thresholds` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`sensor_name` TEXT NOT NULL, " +
                    "`threshold_very_low` INTEGER NOT NULL, " +
                    "`threshold_low` INTEGER NOT NULL, " +
                    "`threshold_medium` INTEGER NOT NULL, " +
                    "`threshold_high` INTEGER NOT NULL, " +
                    "`threshold_very_high` INTEGER NOT NULL " +
                ")"
        )
        database.execSQL("CREATE INDEX `index_sensor_thresholds_sensor_name` ON `sensor_thresholds` (`sensor_name`)")
    }
}

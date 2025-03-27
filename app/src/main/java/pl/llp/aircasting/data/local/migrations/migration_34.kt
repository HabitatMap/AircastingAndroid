package pl.llp.aircasting.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: First ensure all null values in is_external are updated to false (0)
        database.execSQL("UPDATE sessions SET is_external = 0 WHERE is_external IS NULL")

        // Step 2: Create a temporary table with the desired schema
        database.execSQL("""
            CREATE TABLE sessions_temp (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                type INTEGER NOT NULL,
                device_id TEXT,
                device_type INTEGER,
                name TEXT NOT NULL,
                tags TEXT NOT NULL,
                start_time INTEGER NOT NULL,
                end_time INTEGER,
                latitude REAL,
                longitude REAL,
                status INTEGER NOT NULL,
                version INTEGER NOT NULL,
                deleted INTEGER NOT NULL,
                followed_at INTEGER,
                contribute INTEGER NOT NULL,
                locationless INTEGER NOT NULL,
                url_location TEXT,
                is_indoor INTEGER NOT NULL,
                averaging_frequency INTEGER NOT NULL,
                session_order INTEGER,
                username TEXT,
                is_external INTEGER NOT NULL DEFAULT 0
            )
        """)

        // Step 3: Copy data from the original table to the temporary table
        database.execSQL("""
            INSERT INTO sessions_temp 
            SELECT 
                id, uuid, type, device_id, device_type, name, tags, start_time, end_time, 
                latitude, longitude, status, version, deleted, followed_at, contribute, 
                locationless, url_location, is_indoor, averaging_frequency, session_order, 
                username, COALESCE(is_external, 0) 
            FROM sessions
        """)

        // Step 4: Drop the original table
        database.execSQL("DROP TABLE sessions")

        // Step 5: Rename the temporary table to the original table name
        database.execSQL("ALTER TABLE sessions_temp RENAME TO sessions")

        // Step 6: Recreate the indices
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_device_id ON sessions(device_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_session_order ON sessions(session_order)")
    }
}
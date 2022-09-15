package pl.llp.aircasting

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.data.local.migrations.MIGRATION_31_32
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RWQuoteMigrationTest {

    private lateinit var db: SupportSQLiteDatabase

    companion object {
        private const val TEST_DB = "aircasting"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migration31to32() {
        db = helper.createDatabase(TEST_DB, 31)
        db = helper.runMigrationsAndValidate(TEST_DB, 32, true, MIGRATION_31_32)
    }
}

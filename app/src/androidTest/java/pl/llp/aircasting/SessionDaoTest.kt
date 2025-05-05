package pl.llp.aircasting

import android.util.Log
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.serpro69.kfaker.Faker
import io.github.serpro69.kfaker.provider.misc.FallbackStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.data.local.dao.MeasurementDao
import pl.llp.aircasting.data.local.dao.MeasurementStreamDao
import pl.llp.aircasting.data.local.dao.SessionDao
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.local.entity.MeasurementStreamDBObject
import pl.llp.aircasting.data.local.entity.SessionDBObject
import java.util.Date
import kotlin.math.min

@RunWith(AndroidJUnit4::class)
class SessionDaoStressTest {
    companion object {
        private const val DB_NAME = "stress-test-db"
        private const val UUID = "test-uuid"
    }

    private lateinit var database: AppDatabase
    private lateinit var sessionDao: SessionDao
    private lateinit var measurementStreamDao: MeasurementStreamDao
    private lateinit var measurementsDao: MeasurementDao

    private val faker = Faker()

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        ).build()

        val dbFile = context.getDatabasePath(DB_NAME)

        if (dbFile.exists()) {
            val requiredSizeInMB = 50
            val sizeInMb = dbFile.length() / (1024.0 * 1024.0)
            Log.d("SessionDaoStressTest", "Database size: $sizeInMb MB")
            if (sizeInMb < requiredSizeInMB) deleteDatabase()
        } else {
            Log.d("SessionDaoStressTest", "Database file doesn't exist")
        }

        sessionDao = database.sessions()
        measurementStreamDao = database.measurementStreams()
        measurementsDao = database.measurements()
    }

    private fun deleteDatabase() {
        database.close()
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(DB_NAME)
    }

    @Test
    fun testLargeDatasetLoading() {
        runBlocking {
            val measurementsPerStream = 50000
            val numberOfStreams = 5

            measurementsDao.getAll().size.let {
                Log.d("SessionDaoStressTest", "measurements size: $it")
                if (it == 0) {
                    Log.d("SessionDaoStressTest", "setting up new DB")
                    setupLargeTestDataset(
                        measurementsPerStream = measurementsPerStream,
                        streams = numberOfStreams
                    )

                }
            }

            val runtime = Runtime.getRuntime()
            System.gc()

            val startMemory = runtime.totalMemory() - runtime.freeMemory()
            val startTime = System.currentTimeMillis()

            try {
                launch {
                    withContext(Dispatchers.Main) {
                        sessionDao.loadLiveDataSessionForUploadByUUID(UUID).observeForever {
                            Log.d(
                                "Observer",
                                "Measurements size: ${it?.streams?.first()?.measurements?.size}"
                            )
                        }
                    }
                }
                val result = sessionDao.loadSessionAndMeasurementsByUUID(UUID)
                val job2 = launch { sessionDao.reloadSessionAndMeasurementsByUUID(UUID) }
                val job3 = launch { sessionDao.loadCompleteSession(UUID) }

                job2.join()
                job3.join()

                assertNotNull("Session should be loaded", result)
                assertEquals("Should have 5 streams", numberOfStreams, result?.streams?.size)
                assertEquals(
                    "Should have ${numberOfStreams * measurementsPerStream} total measurements",
                    numberOfStreams * measurementsPerStream,
                    result?.measurementsCount
                )

                Log.d(
                    "StressTest",
                    "Successfully loaded session with ${result?.measurementsCount} measurements"
                )
            } catch (e: Exception) {
                Log.e("StressTest", "Exception during test: ${e.message}", e)
                fail("Query failed with exception: ${e.message}")
            } finally {
                val endTime = System.currentTimeMillis()
                val endMemory = runtime.totalMemory() - runtime.freeMemory()

                Log.d("StressTest", "Execution time: ${endTime - startTime}ms")
                Log.d("StressTest", "Memory used: ${(endMemory - startMemory) / 1024 / 1024}MB")
            }
        }
    }

    private suspend fun setupLargeTestDataset(measurementsPerStream: Int, streams: Int) {
        val sessionId = 1L
        val session = faker.randomProvider.randomClassInstance<SessionDBObject> {
            fallbackStrategy = FallbackStrategy.USE_MAX_NUM_OF_ARGS
            namedParameterGenerator(SessionDBObject::uuid.name) { UUID }
            namedParameterGenerator(SessionDBObject::id.name) { sessionId }
            namedParameterGenerator(SessionDBObject::deleted.name) { false }
        }
        sessionDao.insert(session)

        for (i in 1..streams) {
            val stream = faker.randomProvider.randomClassInstance<MeasurementStreamDBObject> {
                fallbackStrategy = FallbackStrategy.USE_MAX_NUM_OF_ARGS
                namedParameterGenerator(MeasurementStreamDBObject::sessionId.name) { sessionId }
                namedParameterGenerator(MeasurementStreamDBObject::id.name) { i.toLong() }
            }

            measurementStreamDao.insert(stream)
            val batchSize = 100
            for (batch in 0 until measurementsPerStream step batchSize) {
                val measurements =
                    (batch until min(batch + batchSize, measurementsPerStream)).map { j ->
                        faker.randomProvider.randomClassInstance<MeasurementDBObject> {
                            fallbackStrategy = FallbackStrategy.USE_MAX_NUM_OF_ARGS
                            namedParameterGenerator(MeasurementDBObject::id.name) { (i * 1000000L) + j }
                            namedParameterGenerator(MeasurementDBObject::sessionId.name) { sessionId }
                            namedParameterGenerator(MeasurementDBObject::measurementStreamId.name) { stream.id }
                            namedParameterGenerator(MeasurementDBObject::value.name) { j.toDouble() }
                            namedParameterGenerator(MeasurementDBObject::time.name) { Date(System.currentTimeMillis() + j) }
                        }
                    }
                measurementsDao.insertAll(measurements)
            }
        }
    }
}
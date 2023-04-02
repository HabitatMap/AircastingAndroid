package pl.llp.aircasting.util.helpers.services

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.extensions.addHours
import pl.llp.aircasting.util.extensions.addSeconds
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.utilities.StubData
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
internal class AveragingServiceTest {
    private val sessionId = 210L
    private val sessionUuid = "uuid"
    private val sessionStartTime = Date(1649310342000L)
    private val streamId = 598L
    private val helper = MeasurementsAveragingHelperDefault()
    private val testScope = TestScope()
    private var sessionAveragingFrequency = AveragingWindow.SECOND.value
    private lateinit var dbSession: SessionDBObject
    private lateinit var sessionsRepository: SessionsRepository
    private lateinit var streamsRepository: MeasurementStreamsRepository
    private lateinit var dbMeasurementsRH: MutableList<MeasurementDBObject>
    private lateinit var db: MutableMap<Long, MeasurementDBObject>
    private lateinit var measurementsRepository: MeasurementsRepositoryImpl
    

    @Before
    fun setup() = runBlocking {
        dbMeasurementsRH =
            StubData.dbMeasurementsFrom("60MeasurementsRHwithAveragingFrequency60.csv")
        dbSession = mock {
            on { id } doReturn sessionId
            on { uuid } doReturn sessionUuid
            on { startTime } doReturn sessionStartTime
            on { averaging_frequency } doReturn sessionAveragingFrequency
        }
        sessionsRepository = mock {
            onBlocking { getSessionByIdSuspend(sessionId) } doReturn dbSession
        }
        streamsRepository = mock {
            onBlocking { getStreamsIdsBySessionIds(listOf(sessionId)) } doReturn listOf(streamId)
        }
        measurementsRepository = mock {
            onBlocking {
                getNonAveragedCurrentMeasurements(
                    eq(streamId),
                    any(),
                    any()
                )
            } doReturn dbMeasurementsRH
            onBlocking { averageMeasurement(any(), any(), any(), any()) } doAnswer {
                val id = it.getArgument(0) as Long
                val average = it.getArgument(1) as Double
                val time = it.getArgument(3) as Date
                db[id] = db[id]!!.copy(value = average, time = time)
            }
            onBlocking { deleteMeasurements(any(), any()) } doAnswer { invocationOnMock ->
                val ids = invocationOnMock.getArgument(1, List::class.java) as List<Long>
                ids.forEach { db.remove(it) }
            }
            onBlocking { lastMeasurementTime(sessionId) } doReturn
                    calendar().addHours(sessionStartTime, 10)
        }
        db = dbMeasurementsRH.associateBy { it.id }.toMutableMap()
    }
    
    @Test
    fun perform_whenAveragingFrequencyIs60_calculatesAveragingCorrectly() = runTest {
        val averagingService = AveragingService(
            measurementsRepository,
            streamsRepository,
            sessionsRepository,
            helper,
            testScope
        )
        val expectedAverage = dbMeasurementsRH.map { it.value }.toTypedArray().average()

        averagingService.stopAndPerformFinalAveraging(sessionUuid)

        assertEquals(1, db.size)
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.value })
    }

    @Test
    fun perform_whenAveragingIsFinal_removesTrailingMeasurements() = runTest {
        setStreamMeasurementsFromFile("119MeasurementsRHwithAveragingFrequency60.csv")
        val averagingService = AveragingService(
            measurementsRepository,
            streamsRepository,
            sessionsRepository,
            helper,
            testScope
        )
        val expectedAverage = dbMeasurementsRH
            .map { it.value }
            .take(sessionAveragingFrequency)
            .toTypedArray()
            .average()

        averagingService.stopAndPerformFinalAveraging(sessionUuid)

        assertEquals(1, db.size)
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.value })
    }

    @Test
    fun perform_whenAveragingIsNotFinal_doesNotRemoveTrailingMeasurements() = runTest {
        setStreamMeasurementsFromFile("119MeasurementsRHwithAveragingFrequency60.csv")
        val averagingService = AveragingService(
            measurementsRepository,
            streamsRepository,
            sessionsRepository,
            helper,
            testScope
        )
        val expectedAverage = dbMeasurementsRH
            .map { it.value }
            .take(sessionAveragingFrequency)
            .toTypedArray()
            .average()

        averagingService.stopAndPerformFinalAveraging(sessionUuid)

        assertEquals(60, db.size)
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.value })
    }

    @Test
    fun perform_whenAveragingFrequencyIs5_calculatesAveragingCorrectly() = runTest {
        setFirstAveragingFrequency()
        val averagingService = AveragingService(
            measurementsRepository,
            streamsRepository,
            sessionsRepository,
            helper,
            testScope
        )
        val expectedAverages = dbMeasurementsRH
            .map { it.value }
            .chunked(AveragingWindow.FIRST.value)
            .map { it.toTypedArray().average() }

        averagingService.stopAndPerformFinalAveraging(sessionUuid, AveragingWindow.FIRST)

        assertEquals(expectedAverages.size, db.size)
        expectedAverages.forEach { averageValue ->
            assertNotNull(db.values.find { it.value == averageValue })
        }
    }

    @Test
    fun perform_whenAveragingFrequencyIs60_keepsMeasurements60SecondsApartFromEachOther() = runTest {
        val expectedDifference = 60 * 1000L
        setStreamMeasurementsFromFile("HabitatHQ-RH-15-hours-of-measurements.csv")
        val averagingService = AveragingService(
            measurementsRepository,
            streamsRepository,
            sessionsRepository,
            helper,
            testScope
        )
        val expectedAveragedSize = 900 / AveragingWindow.SECOND.value
        val expectedFirstMeasurementTime =
            calendar().addSeconds(sessionStartTime, AveragingWindow.SECOND.value)

        averagingService.stopAndPerformFinalAveraging(sessionUuid)

        val averaged = db.values.toMutableList()
        for (i in 1 until averaged.size) {
            val timeDifference = averaged[i].time.time - averaged[i - 1].time.time
            assertEquals(
                expectedDifference,
                timeDifference,
                "Conflicting pair:\n${averaged[i].time}\n${averaged[i - 1].time}\n"
            )
        }
        assertEquals(expectedAveragedSize, averaged.size)
        assertEquals(expectedFirstMeasurementTime, averaged.first().time)
    }

    @Test
    fun perform_whenAveragingFrequencyIs5_keepsMeasurements5SecondsApartFromEachOther() = runTest {
        val expectedDifference = 5 * 1000L
        setStreamMeasurementsFromFile("HabitatHQ-RH-15-hours-of-measurements.csv")
        setFirstAveragingFrequency()
        val averagingService = AveragingService(
            measurementsRepository,
            streamsRepository,
            sessionsRepository,
            helper,
            testScope
        )
        val expectedAveragedSize = 900 / AveragingWindow.FIRST.value
        val expectedFirstMeasurementTime =
            calendar().addSeconds(sessionStartTime, AveragingWindow.FIRST.value)

        averagingService.stopAndPerformFinalAveraging(sessionUuid)

        val averaged = db.values.toMutableList()
        for (i in 1 until averaged.size) {
            val timeDifference = averaged[i].time.time - averaged[i - 1].time.time
            assertEquals(
                expectedDifference,
                timeDifference,
                "Conflicting pair:\n${averaged[i].time}\n${averaged[i - 1].time}\n"
            )
        }
        assertEquals(expectedAveragedSize, averaged.size)
        assertEquals(expectedFirstMeasurementTime, averaged.first().time)
    }

    @Test
    fun averagePreviousMeasurementsWithNewFrequency_removesTrailingMeasurements() = runTest {
        setStreamMeasurementsFromFile("119MeasurementsRHwithAveragingFrequency60.csv")
        setupDbForAveragingPreviousMeasurements(AveragingWindow.ZERO.value)
        whenever(sessionsRepository.getSessionByIdSuspend(sessionId)).thenReturn(dbSession)
        val averagingService = AveragingService(
            measurementsRepository,
            streamsRepository,
            sessionsRepository,
            helper,
            testScope
        )
        val expectedAverage = dbMeasurementsRH
            .map { it.value }
            .take(sessionAveragingFrequency)
            .toTypedArray()
            .average()

        averagingService.stopAndPerformFinalAveraging(sessionUuid)

        assertEquals(1, db.size, db.toString())
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.value })
    }

    @Test
    fun averagePreviousMeasurementsWithNewFrequency_whenAveragingFrequencyChanges_calculatesNewWindowBasedOnFrequencyRatio() = runTest {
        setStreamMeasurementsFromFile("119MeasurementsRHwithAveragingFrequency60.csv")
        setupDbForAveragingPreviousMeasurements(AveragingWindow.FIRST.value)
        val averagingService = AveragingService(
            measurementsRepository,
            streamsRepository,
            sessionsRepository,
            helper,
            testScope
        )
        val newAveragingFrequency = AveragingWindow.SECOND.value / AveragingWindow.FIRST.value
        val expectedAverages = dbMeasurementsRH
            .map { it.value }
            .chunked(newAveragingFrequency)
            .map { it.toTypedArray().average() }
        val expectedAveragedDBSize = db.size / newAveragingFrequency

        averagingService.stopAndPerformFinalAveraging(sessionUuid)

        assertEquals(expectedAveragedDBSize, db.size, db.toString())
        expectedAverages.forEach { averageValue ->
            assertNotNull(db.values.find { it.value == averageValue })
        }
    }

    // TODO: check averages time differences when performing final averaging after sync
    @Test
    fun performFinalAveragingAfterSDSync_calculatesAveragesAccordingToPassedFrequency() = runTest {
        val window = AveragingWindow.FIRST
        val frequency = window.value
        val expectedDifference = frequency * 1000L
        setStreamMeasurementsFromFile("HabitatHQ-RH-15-hours-of-measurements.csv")
        setDefaultAveragingFrequency()

        val averagingService = AveragingService(
            measurementsRepository,
            streamsRepository,
            sessionsRepository,
            helper,
            testScope
        )
        val expectedAveragedSize = 900 / frequency
        val expectedFirstMeasurementTime =
            calendar().addSeconds(sessionStartTime, frequency)

        averagingService.stopAndPerformFinalAveraging(sessionUuid, window)

        val averaged = db.values.toMutableList()
        for (i in 1 until averaged.size) {
            val timeDifference = averaged[i].time.time - averaged[i - 1].time.time
            assertEquals(
                expectedDifference,
                timeDifference,
                "Conflicting pair:\n${averaged[i].time}\n${averaged[i - 1].time}\n"
            )
        }
        assertEquals(expectedAveragedSize, averaged.size)
        assertEquals(expectedFirstMeasurementTime, averaged.first().time)
    }

    @Test
    fun performFinalAveragingAfterSDSync_removesTrailingMeasurements() = runTest {
        val frequency = AveragingWindow.SECOND.value
        setStreamMeasurementsFromFile("119MeasurementsRHwithAveragingFrequency60.csv")
        setupDbForAveragingPreviousMeasurements(AveragingWindow.ZERO.value)
        setDefaultAveragingFrequency()
        whenever(sessionsRepository.getSessionByIdSuspend(sessionId)).thenReturn(dbSession)
        val averagingService = AveragingService(
            measurementsRepository,
            streamsRepository,
            sessionsRepository,
            helper,
            testScope
        )
        val expectedAverage = dbMeasurementsRH
            .map { it.value }
            .take(frequency)
            .toTypedArray()
            .average()

        averagingService.stopAndPerformFinalAveraging(sessionUuid)

        assertEquals(1, db.size, db.toString())
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.value })
    }

    private fun setupDbForAveragingPreviousMeasurements(frequency: Int) = runBlocking {
        whenever(
            measurementsRepository.getNonAveragedPreviousMeasurementsCount(any(), any(), any())
        ).thenReturn(db.size)
        whenever(measurementsRepository.getNonAveragedPreviousMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
        dbSession = mock {
            on { id } doReturn sessionId
            on { startTime } doReturn sessionStartTime
            on { averaging_frequency } doReturn frequency
        }
        whenever(sessionsRepository.getSessionByIdSuspend(sessionId)).thenReturn(dbSession)
    }

    private fun setStreamMeasurementsFromFile(fileName: String) = runBlocking {
        dbMeasurementsRH =
            StubData.dbMeasurementsFrom(fileName)
        db = dbMeasurementsRH.associateBy { it.id }.toMutableMap()
        whenever(measurementsRepository.getNonAveragedCurrentMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
    }

    private fun setFirstAveragingFrequency() = runBlocking {
        whenever(measurementsRepository.lastMeasurementTime(sessionId)).doReturn(
            calendar().addHours(
                sessionStartTime,
                3
            )
        )
    }

    private fun setDefaultAveragingFrequency() = runBlocking {
        whenever(measurementsRepository.lastMeasurementTime(sessionId)).doReturn(sessionStartTime)
    }
}
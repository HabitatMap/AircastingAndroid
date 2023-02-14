package pl.llp.aircasting.util.helpers.services

import org.junit.After
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
import pl.llp.aircasting.util.helpers.services.AveragingService.Companion.FIRST_THRESHOLD_FREQUENCY
import pl.llp.aircasting.util.helpers.services.AveragingService.Companion.SECOND_THRESHOLD_FREQUENCY
import pl.llp.aircasting.utilities.StubData
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class AveragingServiceTest {
    private val sessionId = 210L
    private var sessionStartTime = Date(1649310342000L)
    private var sessionAveragingFrequency = SECOND_THRESHOLD_FREQUENCY
    private lateinit var dbSession: SessionDBObject
    private lateinit var sessionsRepository: SessionsRepository
    private val streamId = 598L
    private lateinit var streamsRepository: MeasurementStreamsRepository
    private val dbMeasurementsRH =
        StubData.dbMeasurementsFrom("60MeasurementsRHwithAveragingFrequency60.csv")
    private lateinit var db: MutableMap<Long, MeasurementDBObject>
    private lateinit var measurementsRepository: MeasurementsRepositoryImpl

    @Before
    fun setup() {
        dbSession = mock {
            on { id } doReturn sessionId
            on { startTime } doReturn sessionStartTime
            on { averaging_frequency } doReturn sessionAveragingFrequency
        }
        sessionsRepository = mock {
            on { getSessionById(sessionId) } doReturn dbSession
        }
        streamsRepository = mock {
            on { getStreamsIdsBySessionIds(listOf(sessionId)) } doReturn listOf(streamId)
        }
        measurementsRepository = mock {
            on {
                getNonAveragedCurrentMeasurements(
                    eq(streamId),
                    any(),
                    any()
                )
            } doReturn dbMeasurementsRH
            on { averageMeasurement(any(), any(), any(), any()) } doAnswer {
                val id = it.getArgument(0) as Long
                val average = it.getArgument(1) as Double
                val time = it.getArgument(3) as Date
                db[id] = db[id]!!.copy(value = average, time = time)
            }
            on { deleteMeasurements(any(), any()) } doAnswer { invocationOnMock ->
                val ids = invocationOnMock.getArgument(1, List::class.java) as List<Long>
                ids.forEach { db.remove(it) }
            }
            on { lastMeasurementTime(sessionId) } doReturn
                    calendar().addHours(sessionStartTime, 10)
        }
        db = dbMeasurementsRH.associateBy { it.id }.toMutableMap()
    }

    @After
    fun tearDown() {
        AveragingService.destroy(sessionId)
    }

    @Test
    fun perform_whenAveragingFrequencyIs60_calculatesAveragingCorrectly() {
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAverage = dbMeasurementsRH.map { it.value }.toTypedArray().average()

        averagingService.perform()

        assertEquals(1, db.size)
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.value })
    }

    @Test
    fun perform_whenAveragingIsFinal_removesTrailingMeasurements() {
        val dbMeasurementsRH =
            StubData.dbMeasurementsFrom("119MeasurementsRHwithAveragingFrequency60.csv")
        db = dbMeasurementsRH.associateBy { it.id }.toMutableMap()
        whenever(measurementsRepository.getNonAveragedCurrentMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAverage = dbMeasurementsRH
            .map { it.value }
            .take(sessionAveragingFrequency)
            .toTypedArray()
            .average()

        averagingService.perform(isFinal = true)

        assertEquals(1, db.size)
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.value })
    }

    @Test
    fun perform_whenAveragingIsNotFinal_doesNotRemoveTrailingMeasurements() {
        val dbMeasurementsRH =
            StubData.dbMeasurementsFrom("119MeasurementsRHwithAveragingFrequency60.csv")
        db = dbMeasurementsRH.associateBy { it.id }.toMutableMap()
        whenever(measurementsRepository.getNonAveragedCurrentMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAverage = dbMeasurementsRH
            .map { it.value }
            .take(sessionAveragingFrequency)
            .toTypedArray()
            .average()

        averagingService.perform(isFinal = false)

        assertEquals(60, db.size)
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.value })
    }

    @Test
    fun perform_whenAveragingFrequencyIs5_calculatesAveragingCorrectly() {
        whenever(measurementsRepository.lastMeasurementTime(sessionId)).doReturn(
            calendar().addHours(sessionStartTime, 3)
        )
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAverages = dbMeasurementsRH
            .map { it.value }
            .chunked(FIRST_THRESHOLD_FREQUENCY)
            .map { it.toTypedArray().average() }

        averagingService.perform()

        assertEquals(expectedAverages.size, db.size)
        expectedAverages.forEach { averageValue ->
            assertNotNull(db.values.find { it.value == averageValue })
        }
    }

    @Test
    fun perform_whenAveragingFrequencyIs60_keepsMeasurements60SecondsApartFromEachOther() {
        val expectedDifference = 60 * 1000L
        val dbMeasurementsRH =
            StubData.dbMeasurementsFrom("HabitatHQ-RH-15-hours-of-measurements.csv")
        db = dbMeasurementsRH.associateBy { it.id }.toMutableMap()
        whenever(measurementsRepository.getNonAveragedCurrentMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAveragedSize = 900 / SECOND_THRESHOLD_FREQUENCY
        val expectedFirstMeasurementTime =
            calendar().addSeconds(sessionStartTime, SECOND_THRESHOLD_FREQUENCY)

        averagingService.perform()

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
        averaged.forEach {
            println(it.time)
        }
    }

    @Test
    fun perform_whenAveragingFrequencyIs5_keepsMeasurements5SecondsApartFromEachOther() {
        val expectedDifference = 5 * 1000L
        val dbMeasurementsRH =
            StubData.dbMeasurementsFrom("HabitatHQ-RH-15-hours-of-measurements.csv")
        db = dbMeasurementsRH.associateBy { it.id }.toMutableMap()
        whenever(measurementsRepository.getNonAveragedCurrentMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
        whenever(measurementsRepository.lastMeasurementTime(sessionId)).doReturn(calendar().addHours(sessionStartTime, 3))
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAveragedSize = 900 / FIRST_THRESHOLD_FREQUENCY
        val expectedFirstMeasurementTime =
            calendar().addSeconds(sessionStartTime, FIRST_THRESHOLD_FREQUENCY)

        averagingService.perform()

        val averaged = db.values.toMutableList()
        averaged.forEach {
            println(it.time)
        }
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
    fun averagePreviousMeasurementsWithNewFrequency_removesTrailingMeasurements() {
        val dbMeasurementsRH =
            StubData.dbMeasurementsFrom("119MeasurementsRHwithAveragingFrequency60.csv")
        db = dbMeasurementsRH.associateBy { it.id }.toMutableMap()
        whenever(measurementsRepository.getNonAveragedCurrentMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
        whenever(
            measurementsRepository.getNonAveragedPreviousMeasurementsCount(
                any(),
                any(),
                any()
            )
        ).thenReturn(db.size)
        whenever(measurementsRepository.getNonAveragedPreviousMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
        dbSession = mock {
            on { id } doReturn sessionId
            on { startTime } doReturn sessionStartTime
            on { averaging_frequency } doReturn AveragingService.DEFAULT_FREQUENCY
        }
        whenever(sessionsRepository.getSessionById(sessionId)).thenReturn(dbSession)
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAverage = dbMeasurementsRH
            .map { it.value }
            .take(sessionAveragingFrequency)
            .toTypedArray()
            .average()

        averagingService.averagePreviousMeasurementsWithNewFrequency()

        assertEquals(1, db.size, db.toString())
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.value })
    }

    @Test
    fun averagePreviousMeasurementsWithNewFrequency_whenAveragingFrequencyChanges_calculatesNewWindowBasedOnFrequencyRatio() {
        val dbMeasurementsRH =
            StubData.dbMeasurementsFrom("119MeasurementsRHwithAveragingFrequency60.csv")
        db = dbMeasurementsRH.associateBy { it.id }.toMutableMap()
        whenever(measurementsRepository.getNonAveragedCurrentMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
        whenever(
            measurementsRepository.getNonAveragedPreviousMeasurementsCount(any(), any(), any())
        ).thenReturn(db.size)
        whenever(measurementsRepository.getNonAveragedPreviousMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
        dbSession = mock {
            on { id } doReturn sessionId
            on { startTime } doReturn sessionStartTime
            on { averaging_frequency } doReturn FIRST_THRESHOLD_FREQUENCY
        }
        whenever(sessionsRepository.getSessionById(sessionId)).thenReturn(dbSession)
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val newAveragingFrequency = SECOND_THRESHOLD_FREQUENCY / FIRST_THRESHOLD_FREQUENCY
        val expectedAverages = dbMeasurementsRH
            .map { it.value }
            .chunked(newAveragingFrequency)
            .map { it.toTypedArray().average() }
        val expectedAveragedDBSize = db.size / newAveragingFrequency

        averagingService.averagePreviousMeasurementsWithNewFrequency()

        assertEquals(expectedAveragedDBSize, db.size, db.toString())
        expectedAverages.forEach { averageValue ->
            assertNotNull(db.values.find { it.value == averageValue })
        }
    }
}
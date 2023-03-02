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
import pl.llp.aircasting.util.helpers.services.AveragingService.Companion.DEFAULT_FREQUENCY
import pl.llp.aircasting.util.helpers.services.AveragingService.Companion.FIRST_THRESHOLD_FREQUENCY
import pl.llp.aircasting.util.helpers.services.AveragingService.Companion.SECOND_THRESHOLD_FREQUENCY
import pl.llp.aircasting.utilities.StubData
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class AveragingServiceTest {
    private val sessionId = 210L
    private val sessionStartTime = Date(1649310342000L)
    private val streamId = 598L
    private var sessionAveragingFrequency = SECOND_THRESHOLD_FREQUENCY
    private lateinit var dbSession: SessionDBObject
    private lateinit var sessionsRepository: SessionsRepository
    private lateinit var streamsRepository: MeasurementStreamsRepository
    private lateinit var dbMeasurementsRH: MutableList<MeasurementDBObject>
    private lateinit var db: MutableMap<Long, MeasurementDBObject>
    private lateinit var measurementsRepository: MeasurementsRepositoryImpl

    @Before
    fun setup() {
        dbMeasurementsRH =
            StubData.dbMeasurementsFrom("60MeasurementsRHwithAveragingFrequency60.csv")
        dbSession = mock {
            on { id } doReturn sessionId
            on { startTime } doReturn sessionStartTime
            on { averaging_frequency } doReturn sessionAveragingFrequency
        }
        sessionsRepository = mock {
            on { getSessionByIdSuspend(sessionId) } doReturn dbSession
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
        val expectedAverage = dbMeasurementsRH.map { it.mValue }.toTypedArray().average()

        averagingService.perform()

        assertEquals(1, db.size)
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.mValue })
    }

    @Test
    fun perform_whenAveragingIsFinal_removesTrailingMeasurements() {
        setStreamMeasurementsFromFile("119MeasurementsRHwithAveragingFrequency60.csv")
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAverage = dbMeasurementsRH
            .map { it.mValue }
            .take(sessionAveragingFrequency)
            .toTypedArray()
            .average()

        averagingService.perform(isFinal = true)

        assertEquals(1, db.size)
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.mValue })
    }

    @Test
    fun perform_whenAveragingIsNotFinal_doesNotRemoveTrailingMeasurements() {
        setStreamMeasurementsFromFile("119MeasurementsRHwithAveragingFrequency60.csv")
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAverage = dbMeasurementsRH
            .map { it.mValue }
            .take(sessionAveragingFrequency)
            .toTypedArray()
            .average()

        averagingService.perform(isFinal = false)

        assertEquals(60, db.size)
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.mValue })
    }

    @Test
    fun perform_whenAveragingFrequencyIs5_calculatesAveragingCorrectly() {
        setFirstAveragingFrequency()
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAverages = dbMeasurementsRH
            .map { it.mValue }
            .chunked(FIRST_THRESHOLD_FREQUENCY)
            .map { it.toTypedArray().average() }

        averagingService.perform()

        assertEquals(expectedAverages.size, db.size)
        expectedAverages.forEach { averageValue ->
            assertNotNull(db.values.find { it.mValue == averageValue })
        }
    }

    @Test
    fun perform_whenAveragingFrequencyIs60_keepsMeasurements60SecondsApartFromEachOther() {
        val expectedDifference = 60 * 1000L
        setStreamMeasurementsFromFile("HabitatHQ-RH-15-hours-of-measurements.csv")
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
    }

    @Test
    fun perform_whenAveragingFrequencyIs5_keepsMeasurements5SecondsApartFromEachOther() {
        val expectedDifference = 5 * 1000L
        setStreamMeasurementsFromFile("HabitatHQ-RH-15-hours-of-measurements.csv")
        setFirstAveragingFrequency()
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
        setStreamMeasurementsFromFile("119MeasurementsRHwithAveragingFrequency60.csv")
        setupDbForAveragingPreviousMeasurements(DEFAULT_FREQUENCY)
        whenever(sessionsRepository.getSessionByIdSuspend(sessionId)).thenReturn(dbSession)
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAverage = dbMeasurementsRH
            .map { it.mValue }
            .take(sessionAveragingFrequency)
            .toTypedArray()
            .average()

        averagingService.averagePreviousMeasurementsWithNewFrequency()

        assertEquals(1, db.size, db.toString())
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.mValue })
    }

    @Test
    fun averagePreviousMeasurementsWithNewFrequency_whenAveragingFrequencyChanges_calculatesNewWindowBasedOnFrequencyRatio() {
        setStreamMeasurementsFromFile("119MeasurementsRHwithAveragingFrequency60.csv")
        setupDbForAveragingPreviousMeasurements(FIRST_THRESHOLD_FREQUENCY)
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val newAveragingFrequency = SECOND_THRESHOLD_FREQUENCY / FIRST_THRESHOLD_FREQUENCY
        val expectedAverages = dbMeasurementsRH
            .map { it.mValue }
            .chunked(newAveragingFrequency)
            .map { it.toTypedArray().average() }
        val expectedAveragedDBSize = db.size / newAveragingFrequency

        averagingService.averagePreviousMeasurementsWithNewFrequency()

        assertEquals(expectedAveragedDBSize, db.size, db.toString())
        expectedAverages.forEach { averageValue ->
            assertNotNull(db.values.find { it.mValue == averageValue })
        }
    }

    // TODO: check averages time differences when performing final averaging after sync
    @Test
    fun performFinalAveragingAfterSDSync_calculatesAveragesAccordingToPassedFrequency() {
        val frequency = FIRST_THRESHOLD_FREQUENCY
        val expectedDifference = frequency * 1000L
        setStreamMeasurementsFromFile("HabitatHQ-RH-15-hours-of-measurements.csv")
        setDefaultAveragingFrequency()

        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAveragedSize = 900 / frequency
        val expectedFirstMeasurementTime =
            calendar().addSeconds(sessionStartTime, frequency)

        averagingService.performFinalAveragingAfterSDSync(frequency)

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
    fun performFinalAveragingAfterSDSync_removesTrailingMeasurements() {
        val frequency = SECOND_THRESHOLD_FREQUENCY
        setStreamMeasurementsFromFile("119MeasurementsRHwithAveragingFrequency60.csv")
        setupDbForAveragingPreviousMeasurements(DEFAULT_FREQUENCY)
        setDefaultAveragingFrequency()
        whenever(sessionsRepository.getSessionByIdSuspend(sessionId)).thenReturn(dbSession)
        val averagingService = AveragingService.get(
            sessionId,
            measurementsRepository,
            streamsRepository,
            sessionsRepository
        )!!
        val expectedAverage = dbMeasurementsRH
            .map { it.mValue }
            .take(frequency)
            .toTypedArray()
            .average()

        averagingService.performFinalAveragingAfterSDSync(frequency)

        assertEquals(1, db.size, db.toString())
        assertEquals(expectedAverage, db.firstNotNullOf { it.value.mValue })
    }

    private fun setupDbForAveragingPreviousMeasurements(frequency: Int) {
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

    private fun setStreamMeasurementsFromFile(fileName: String) {
        dbMeasurementsRH =
            StubData.dbMeasurementsFrom(fileName)
        db = dbMeasurementsRH.associateBy { it.id }.toMutableMap()
        whenever(measurementsRepository.getNonAveragedCurrentMeasurements(any(), any(), any()))
            .thenReturn(dbMeasurementsRH)
    }

    private fun setFirstAveragingFrequency() {
        whenever(measurementsRepository.lastMeasurementTime(sessionId)).doReturn(
            calendar().addHours(
                sessionStartTime,
                3
            )
        )
    }

    private fun setDefaultAveragingFrequency() {
        whenever(measurementsRepository.lastMeasurementTime(sessionId)).doReturn(sessionStartTime)
    }
}
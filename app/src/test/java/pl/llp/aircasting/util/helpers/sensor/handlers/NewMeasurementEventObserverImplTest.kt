package pl.llp.aircasting.util.helpers.sensor.handlers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class NewMeasurementEventObserverImplTest {
    private val testScope = TestScope()

    @Mock
    lateinit var settings: Settings

    @Mock
    lateinit var sessionsRepository: SessionsRepository

    @Mock
    lateinit var activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository

    @Mock
    lateinit var errorHandler: ErrorHandler

    @Mock
    lateinit var measurementStreamsRepository: MeasurementStreamsRepository

    @Mock
    lateinit var measurementsRepository: MeasurementsRepositoryImpl

    @Captor
    lateinit var measurementCaptor: ArgumentCaptor<Measurement>

    private lateinit var observer: NewMeasurementEventObserver

    @Before
    fun setup() {
        observer = NewMeasurementEventObserverImpl(
            settings,
            errorHandler,
            sessionsRepository,
            measurementStreamsRepository,
            measurementsRepository,
            activeSessionMeasurementsRepository
        )
    }

    @Test
    fun observe_savesMeasurementAndStreamToDB() = testScope.runTest {
        val deviceId = "123"
        val sessionId = 2L
        whenever(sessionsRepository.getMobileActiveSessionIdByDeviceId(deviceId))
            .thenReturn(sessionId)
        val streamId = 1L
        whenever(measurementStreamsRepository.getIdOrInsertSuspend(any(), any()))
            .thenReturn(streamId)
        val event = newMeasurementEvent(deviceId = deviceId)
        val stream = MeasurementStream(event)
        val flow = MutableSharedFlow<NewMeasurementEvent>()

        observer.observe(flow, testScope, 5)
        yield()
        flow.emit(event)
        yield()

        verify(measurementStreamsRepository).getIdOrInsertSuspend(eq(sessionId), eq(stream))
        verify(measurementsRepository).insert(
            eq(streamId),
            eq(sessionId),
            capture(measurementCaptor)
        )
        val measurement = measurementCaptor.value
        assertEquals(event.measuredValue, measurement.value)
        verify(activeSessionMeasurementsRepository).createOrReplace(
            sessionId,
            streamId,
            measurement
        )
        coroutineContext.cancelChildren()
    }

    @Test
    fun observe_setsIdenticalTimeAndLocationToMeasurementsInOneSet() = testScope.runTest {
        val db = mutableListOf<Measurement>()
        whenever(measurementsRepository.insert(any(), any(), any())).thenAnswer {
            val measurement = it.getArgument(2, Measurement::class.java)
        }
        val events = listOf(
            newMeasurementEvent(1.0),
            newMeasurementEvent(2.0),
            newMeasurementEvent(3.0),
            newMeasurementEvent(4.0),
            newMeasurementEvent(5.0),
            newMeasurementEvent(6.0)
        )
        val flow = MutableSharedFlow<NewMeasurementEvent>()

        observer.observe(flow, testScope, 5)
        yield()
        events.forEach {
            flow.emit(it)
            yield()
            delay(200)
        }
        advanceTimeBy(200L * events.size)


        coroutineContext.cancelChildren()
    }

    private fun newMeasurementEvent(value: Double = 1.0, deviceId: String = "123") =
        NewMeasurementEvent(
            sensorPackageName = "test:$deviceId",
            sensorName = "test2",
            measurementType = "test3",
            measurementShortType = "test4",
            unitName = "test5",
            unitSymbol = "test6",
            thresholdVeryLow = 1,
            thresholdLow = 1,
            thresholdMedium = 1,
            thresholdHigh = 1,
            thresholdVeryHigh = 1,
            measuredValue = value,
        )
}
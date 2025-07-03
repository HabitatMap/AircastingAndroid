package pl.llp.aircasting.util.helpers.sensor.handlers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.llp.aircasting.data.api.services.FixedSessionUploader
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class RecordingHandlerImplTest {

    private val testScope = TestScope()

    @Mock
    lateinit var settings: Settings

    @Mock
    lateinit var fixedSessionUploader: FixedSessionUploader

    @Mock
    lateinit var sessionsRepository: SessionsRepository

    @Mock
    lateinit var activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository

    @Mock
    lateinit var sessionsSyncService: SessionsSyncService

    @Mock
    lateinit var errorHandler: ErrorHandler

    @Mock
    lateinit var measurementStreamsRepository: MeasurementStreamsRepository

    @Mock
    lateinit var measurementsRepository: MeasurementsRepositoryImpl

    @Spy
    private val flows: MutableMap<String, MutableSharedFlow<NewMeasurementEvent>> = mutableMapOf()

    @Spy
    private val observers: MutableMap<String, Job> = mutableMapOf()

    lateinit var recordingHandler: RecordingHandler

    private val testDeviceId = "deviceId"
    private val wifiSSID = "SSID"
    private val wifiPassword = "1234"

    private val mobileSession: Session = mock {
        on { type } doReturn Session.Type.MOBILE
        on { deviceId } doReturn testDeviceId
        on { defaultNumberOfStreams() } doReturn 5
    }
    private val fixedSession: Session = mock {
        on { type } doReturn Session.Type.FIXED
    }

    @Before
    fun setup() {
        recordingHandler = RecordingHandlerImpl(
            settings,
            fixedSessionUploader,
            sessionsRepository,
            activeSessionMeasurementsRepository,
            sessionsSyncService,
            errorHandler,
            measurementStreamsRepository,
            measurementsRepository,
            mock(),
            testScope,
            flows,
            observers
        )
    }

    @Test
    fun startRecording_setsSessionStatus() = testScope.runTest {
        val session = mock<Session> {
            on { type } doReturn Session.Type.FIXED
        }

        recordingHandler.startRecording(session, wifiSSID, wifiPassword)
        yield()

        verify(session).setAppropriateStatusForStartOfRecording()
    }

    @Test
    fun startRecording_insertsSessionToDB() = testScope.runTest {
        recordingHandler.startRecording(fixedSession, wifiSSID, wifiPassword)
        yield()

        verify(sessionsRepository).insert(fixedSession)
    }

    @Test
    fun startRecording_whenSessionIsFixed_setsFollowedAt_increasesCount_uploadsSession() =
        testScope.runTest {
            recordingHandler.startRecording(fixedSession, wifiSSID, wifiPassword)
            yield()

            verify(fixedSession).setFollowedAtNow()
            verify(settings).increaseFollowedSessionsCount()
            verify(fixedSessionUploader).invoke(fixedSession)
        }

    @Ignore("complete after improving average service")
    @Test
    fun startRecording_whenSessionIsMobile_startsAveragingService() =
        testScope.runTest {
            // TODO
        }


    @Test
    fun startRecording_whenSessionIsMobile_addsFlowAndObserver() = testScope.runTest {
        recordingHandler.startRecording(mobileSession, wifiSSID, wifiPassword)
        yield()

        verify(flows)[eq(testDeviceId)] = any()
        verify(observers)[eq(testDeviceId)] = any()
        testScope.coroutineContext.cancelChildren()
    }

    @Test
    fun handle_emitsEventToFlow() = testScope.runTest {
        flows[testDeviceId] = mock()
        val event = mock<NewMeasurementEvent> {
            on { deviceId } doReturn testDeviceId
        }

        recordingHandler.handle(event)
        yield()

        verify(flows[testDeviceId])?.emit(event)
    }

    @Test
    fun stopRecording() = testScope.runTest {
        val sessionId = 1L
        whenever(sessionsRepository.getSessionIdByUUID(any())).thenReturn(sessionId)
        whenever(sessionsRepository.loadSessionAndMeasurementsByUUID(any())).thenReturn(mobileSession)
        flows[testDeviceId] = mock()
        val job = mock<Job>()
        observers[testDeviceId] = job

        recordingHandler.stopRecording("uuid")
        yield()

        verify(mobileSession).stopRecording()
        verify(sessionsRepository).update(mobileSession)
        verify(activeSessionMeasurementsRepository).deleteBySessionId(sessionId)
        verify(sessionsSyncService).sync()
        // TODO: verify averaging services stop
        verify(job).cancel()
        verify(observers).remove(testDeviceId)
        verify(flows).remove(testDeviceId)
    }
}

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.helpers.services.AverageableMeasurement
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.helpers.services.AveragingWindow
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelper
import pl.llp.aircasting.utilities.StubData
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AveragingServiceTest {
    @Captor
    lateinit var callbackCaptor:
            ArgumentCaptor<suspend (AverageableMeasurement, List<AverageableMeasurement>) -> Unit>

    private lateinit var mMeasurementsRepository: MeasurementsRepositoryImpl
    private lateinit var mMeasurementStreamsRepository: MeasurementStreamsRepository
    private lateinit var mSessionsRepository: SessionsRepository
    private lateinit var helper: MeasurementsAveragingHelper
    private lateinit var coroutineScope: TestScope
    private lateinit var sessionUuidByAveragingJob: MutableMap<String, Job>
    private lateinit var averagingService: AveragingService

    @Before
    fun setup() {
        mMeasurementsRepository = mock()
        mMeasurementStreamsRepository = mock()
        mSessionsRepository = mock()
        helper = mock()
        coroutineScope = TestScope(StandardTestDispatcher())
        sessionUuidByAveragingJob = ConcurrentHashMap()

        averagingService = AveragingService(
            mMeasurementsRepository,
            mMeasurementStreamsRepository,
            mSessionsRepository,
            helper,
            coroutineScope,
            sessionUuidByAveragingJob
        )
    }

    @Test
    fun `stopAndPerformFinalAveraging should cancel job and perform averaging`() = coroutineScope.runTest {
        // Arrange
        val uuid = "test_uuid"
        val session = mock<SessionDBObject> {
            on { id } doReturn 1L
            on { startTime } doReturn Date()
            on { this.uuid } doReturn uuid
        }
        val expectedAveragedValue = 59.0
        val averagingWindow = AveragingWindow.SECOND
        val job = mock<Job>()
        val streamIds = listOf(598L)
        val measurements = StubData.dbMeasurementsFrom("60MeasurementsRHwithAveragingFrequency60.csv")
        whenever(mSessionsRepository.getSessionByUUID(uuid)).thenReturn(session)
        sessionUuidByAveragingJob[uuid] = job
        whenever(mMeasurementStreamsRepository.getStreamsIdsBySessionId(session.id)).thenReturn(streamIds)
        whenever(mMeasurementsRepository.getMeasurementsToAverage(any(), any())).thenReturn(measurements)

        // Act
        averagingService.stopAndPerformFinalAveraging(uuid, averagingWindow)
        yield()

        // Assert
        verify(job).cancel()
        verify(mSessionsRepository).getSessionByUUID(uuid)

        streamIds.forEach { streamId ->
            inOrder(mMeasurementStreamsRepository, mMeasurementsRepository, helper) {
                verify(mMeasurementStreamsRepository).getStreamsIdsBySessionId(session.id)
                verify(mMeasurementsRepository).getMeasurementsToAverage(streamId, averagingWindow)
//                TODO: add MockK to use matcher for suspending callback
//                verify(helper).averageMeasurements(
//                    measurements = eq(measurements),
//                    startTime = eq(session.startTime),
//                    averagingWindow = eq(averagingWindow),
//                    callback = any()
//                )
//                verify(mMeasurementsRepository).deleteMeasurements(eq(streamId), any())
//                verify(mMeasurementsRepository).averageMeasurement(any(), eq(expectedAveragedValue), averagingWindow.value, any())
            }
        }

    }

    @Test
    fun `scheduleAveraging should start periodic averaging`() = coroutineScope.runTest {
        // Arrange
        val sessionId = 1L
        val session = mock<SessionDBObject> {
            on { id } doReturn sessionId
            on { startTime } doReturn Date()
            on { uuid } doReturn "uuid"
        }
        whenever(mSessionsRepository.getSessionByIdSuspend(sessionId)).thenReturn(session)

        // Act
        averagingService.scheduleAveraging(sessionId)
        yield()

        // Assert
        verify(mSessionsRepository).getSessionByIdSuspend(sessionId)
    }
}

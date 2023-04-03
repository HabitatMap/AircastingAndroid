
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.helpers.services.AveragingWindow
import pl.llp.aircasting.utilities.StubData
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AveragingServiceTest {
    @Mock
    lateinit var mMeasurementsRepository: MeasurementsRepositoryImpl
    @Mock
    lateinit var mMeasurementStreamsRepository: MeasurementStreamsRepository
    @Mock
    lateinit var mSessionsRepository: SessionsRepository

    lateinit var coroutineScope: TestScope
    lateinit var sessionUuidByAveragingJob: MutableMap<String, Job>
    lateinit var averagingService: AveragingService

    @Before
    fun setup() {
        coroutineScope = TestScope(StandardTestDispatcher())
        sessionUuidByAveragingJob = ConcurrentHashMap()

        averagingService = AveragingService(
            mMeasurementsRepository,
            mMeasurementStreamsRepository,
            mSessionsRepository,
            mock(),
            coroutineScope,
            sessionUuidByAveragingJob
        )
    }

    @Test
    fun `stopAndPerformFinalAveraging should cancel job, perform averaging and delete leftover measurements`() =
        coroutineScope.runTest {
            // Arrange
            val uuid = "test_uuid"
            val session = mock<SessionDBObject> {
                on { id } doReturn 1L
                on { startTime } doReturn Date()
            }
            val averagingWindow = AveragingWindow.SECOND
            val job = mock<Job>()
            val streamIds = listOf(598L)
            val measurements =
                StubData.dbMeasurementsFrom("60MeasurementsRHwithAveragingFrequency60.csv")
            whenever(mSessionsRepository.getSessionByUUID(uuid)).thenReturn(session)
            sessionUuidByAveragingJob[uuid] = job
            whenever(mMeasurementStreamsRepository.getStreamsIdsBySessionId(session.id)).thenReturn(
                streamIds
            )
            whenever(mMeasurementsRepository.getMeasurementsToAverage(any(), any())).thenReturn(
                measurements
            )

            // Act
            averagingService.stopAndPerformFinalAveraging(uuid, averagingWindow)
            yield()

            // Assert
            verify(job).cancel()
            verify(mSessionsRepository).getSessionByUUID(uuid)
            streamIds.forEach { streamId ->
                inOrder(mMeasurementStreamsRepository, mMeasurementsRepository) {
                    verify(mMeasurementStreamsRepository).getStreamsIdsBySessionId(session.id)
                    verify(mMeasurementsRepository).getMeasurementsToAverage(
                        streamId,
                        averagingWindow
                    )
                }
                inOrder(mMeasurementStreamsRepository, mMeasurementsRepository) {
                    verify(mMeasurementsRepository).getMeasurementsToAverage(
                        streamId,
                        averagingWindow
                    )
                    verify(mMeasurementsRepository).deleteMeasurements(eq(streamId), any())
                }
            }
        }

    @Test
    fun `scheduleAveraging should start periodic averaging`() = coroutineScope.runTest {
        // Arrange
        val sessionId = 1L
        val session = mock<SessionDBObject>()
        whenever(mSessionsRepository.getSessionByIdSuspend(sessionId)).thenReturn(session)

        // Act
        averagingService.scheduleAveraging(sessionId)
        yield()

        // Assert
        verify(mSessionsRepository).getSessionByIdSuspend(sessionId)
    }
}

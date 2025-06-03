package pl.llp.aircasting.util.helpers.sensor.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.utilities.dataClassFixture
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSessionFinisherTest {

    @Test
    fun `does not finish session if already finished`() = runTest {
        val uuid = "testUUID"
        val session = mock<SessionDBObject> {
            on(it.uuid) doReturn uuid
            on(it.isNotFinished) doReturn false
        }
        val settings = mock<Settings>()
        val measurementsRepository = mock<MeasurementsRepository>()
        val sessionRepository =
            mock<SessionsRepository> {
                onBlocking { it.getSessionByUUID(uuid) } doReturn session
            }

        val finishSession =
            DefaultSessionFinisher(sessionRepository, settings, measurementsRepository)

        finishSession(uuid)

        verify(sessionRepository, times(0)).update(any<Session>())
        verify(sessionRepository, times(0)).update(any<SessionDBObject>())
        verify(settings, times(0)).decreaseActiveMobileSessionsCount()
        verify(measurementsRepository, times(0)).lastMeasurementTime(any())
    }

    @Test
    fun `finishes session if not already finished`() = runTest {
        val uuid = "testUUID"
        val sessionId = 2L
        val session = dataClassFixture<SessionDBObject> {
            property(SessionDBObject::id) { sessionId }
            property(SessionDBObject::uuid) { uuid }
            property(SessionDBObject::status) { Session.Status.DISCONNECTED }
        }
        val sessionRepository =
            mock<SessionsRepository> {
                onBlocking { it.getSessionByUUID(uuid) } doReturn session
            }
        val settings = mock<Settings>()
        val lastMeasurementTime = Date()
        val measurementsRepository = mock<MeasurementsRepository> {
            onBlocking { it.lastMeasurementTime(sessionId) } doReturn lastMeasurementTime
        }

        val finishSession =
            DefaultSessionFinisher(sessionRepository, settings, measurementsRepository)

        finishSession(uuid)

        verify(measurementsRepository, times(1)).lastMeasurementTime(sessionId)
        verify(sessionRepository, times(1)).update(
            eq(
                session.copy(
                    status = Session.Status.FINISHED,
                    endTime = lastMeasurementTime
                )
            )
        )
        verify(settings, times(1)).decreaseActiveMobileSessionsCount()
    }
}
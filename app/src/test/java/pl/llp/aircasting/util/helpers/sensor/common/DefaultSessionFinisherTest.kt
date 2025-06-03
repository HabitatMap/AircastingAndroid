package pl.llp.aircasting.util.helpers.sensor.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSessionFinisherTest {

    @Test
    fun `does not finish session if already finished`() = runTest {
        val uuid = "testUUID"
        val session = mock<SessionDBObject> {
            on(it.uuid) doReturn uuid
            on(it.isNotFinished) doReturn false
        }
        val sessionRepository =
            mock<SessionsRepository> {
                onBlocking { it.getSessionByUUID(uuid) } doReturn session
            }

        val finishSession = DefaultSessionFinisher(sessionRepository)

        finishSession(uuid)

        verify(sessionRepository, times(0)).updateSessionStatus(any<String>(), any())
        verify(sessionRepository, times(0)).updateSessionStatus(any<Session>(), any())
    }

    @Test
    fun `finishes session if not already finished`() = runTest {
        val uuid = "testUUID"
        val session = mock<SessionDBObject> {
            on(it.uuid) doReturn uuid
            on(it.isNotFinished) doReturn true
        }
        val sessionRepository =
            mock<SessionsRepository> {
                onBlocking { it.getSessionByUUID(uuid) } doReturn session
            }

        val finishSession = DefaultSessionFinisher(sessionRepository)

        finishSession(uuid)

        verify(sessionRepository, times(1)).updateSessionStatus(any<String>(), any())
        verify(sessionRepository, times(0)).updateSessionStatus(any<Session>(), any())
    }
}
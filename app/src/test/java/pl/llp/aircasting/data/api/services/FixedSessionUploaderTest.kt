package pl.llp.aircasting.data.api.services

import android.os.Build
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pl.llp.aircasting.data.api.response.UploadSessionResponse
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.utilities.dataClassFixture
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [Build.VERSION_CODES.S])
@RunWith(RobolectricTestRunner::class)
class FixedSessionUploaderTest {

    private lateinit var apiService: ApiService
    private lateinit var errorHandler: ErrorHandler
    private lateinit var sessionsRepository: SessionsRepository

    @Test
    fun upload_whenSuccessful_updatesSessionUrlLocation() = runTest {
        val locationUrl = "testLocationUrl"
        apiService = mock {
            on(it.createFixedSession(any())) doReturn Response.success(
                UploadSessionResponse(
                    locationUrl
                )
            )
        }
        errorHandler = mock()
        sessionsRepository = mock()
        val session = dataClassFixture<Session>()

        val instance = FixedSessionUploaderDefault(
            apiService,
            errorHandler,
            sessionsRepository,
        )

        instance.invoke(session)

        verify(sessionsRepository).updateUrlLocation(session, locationUrl)
    }
}
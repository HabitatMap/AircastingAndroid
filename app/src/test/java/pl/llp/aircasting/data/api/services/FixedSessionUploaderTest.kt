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
import org.mockito.kotlin.argumentCaptor
import pl.llp.aircasting.data.api.params.CreateFixedSessionV3Body
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
                    locationUrl,
                    null,
                    null
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

    @Test
    fun uploadV2_sendsTimezoneAndOmitsCoordinatesForIndoorSession() = runTest {
        val locationUrl = "testLocationUrl"
        val sessionToken = "1234567890abcdef1234567890abcdef"
        val streams = listOf(
            UploadSessionResponse.StreamInfo("AirBeamMini-PM1", 1),
            UploadSessionResponse.StreamInfo("AirBeamMini-PM2.5", 2)
        )
        apiService = mock {
            on(it.createFixedSessionV3(any())) doReturn Response.success(
                UploadSessionResponse(
                    locationUrl,
                    sessionToken,
                    streams
                )
            )
        }
        errorHandler = mock()
        sessionsRepository = mock()
        val session = Session(
            sessionUUID = "uuid",
            deviceId = "mac",
            deviceType = null,
            mType = Session.Type.FIXED,
            mName = "Title",
            mTags = arrayListOf(),
            mStatus = Session.Status.FINISHED,
            indoor = true,
            streamingMethod = null,
            location = Session.Location(40.7128, -74.0060),
            contribute = true,
            locationless = false
        )

        val instance = FixedSessionUploaderDefault(
            apiService,
            errorHandler,
            sessionsRepository,
        )

        instance.invoke(session, isV2 = true)

        val bodyCaptor = argumentCaptor<CreateFixedSessionV3Body>()
        verify(apiService).createFixedSessionV3(bodyCaptor.capture())

        val body = bodyCaptor.firstValue
        org.junit.Assert.assertNull(body.latitude)
        org.junit.Assert.assertNull(body.longitude)
        org.junit.Assert.assertEquals(java.util.TimeZone.getDefault().id, body.time_zone)
    }

    @Test
    fun uploadV2_sendsCoordinatesForOutdoorSession() = runTest {
        val locationUrl = "testLocationUrl"
        val sessionToken = "1234567890abcdef1234567890abcdef"
        val streams = listOf(
            UploadSessionResponse.StreamInfo("AirBeamMini-PM1", 1),
            UploadSessionResponse.StreamInfo("AirBeamMini-PM2.5", 2)
        )
        apiService = mock {
            on(it.createFixedSessionV3(any())) doReturn Response.success(
                UploadSessionResponse(
                    locationUrl,
                    sessionToken,
                    streams
                )
            )
        }
        errorHandler = mock()
        sessionsRepository = mock()
        val session = Session(
            sessionUUID = "uuid",
            deviceId = "mac",
            deviceType = null,
            mType = Session.Type.FIXED,
            mName = "Title",
            mTags = arrayListOf(),
            mStatus = Session.Status.FINISHED,
            indoor = false,
            streamingMethod = null,
            location = Session.Location(40.7128, -74.0060),
            contribute = true,
            locationless = false
        )

        val instance = FixedSessionUploaderDefault(
            apiService,
            errorHandler,
            sessionsRepository,
        )

        instance.invoke(session, isV2 = true)

        val bodyCaptor = argumentCaptor<CreateFixedSessionV3Body>()
        verify(apiService).createFixedSessionV3(bodyCaptor.capture())

        val body = bodyCaptor.firstValue
        org.junit.Assert.assertEquals(40.7128, body.latitude)
        org.junit.Assert.assertEquals(-74.0060, body.longitude)
        org.junit.Assert.assertEquals(java.util.TimeZone.getDefault().id, body.time_zone)
    }
}
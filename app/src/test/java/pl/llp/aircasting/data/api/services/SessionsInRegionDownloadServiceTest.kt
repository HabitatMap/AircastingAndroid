package pl.llp.aircasting.data.api.services

import com.google.gson.Gson
import okhttp3.ResponseBody.Companion.toResponseBody
import org.mockito.Mockito.any
import org.mockito.kotlin.*
import pl.llp.aircasting.data.api.responses.SessionsInRegionResponse
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.utilities.StubData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.junit.Test
import kotlin.test.assertTrue

class SessionsInRegionDownloadServiceTest {
    private val testSquare = GeoSquare(1.0, 1.0, 1.0, 1.0)
    private val testJson = StubData.getJson("SessionsCracow.json")

    @Test
    fun whenThereAreNoSessionsDownloaded_shouldReturnEmptyList() {
        // when
        val service = SessionsInRegionDownloadService(apiService)

        // then
        assertTrue(service.sessions.isEmpty())
    }

    @Test
    fun whenThereAreSessions_shouldReturnNonEmptyList() {
        // given
        val service = SessionsInRegionDownloadService(apiService)
        val session = mock(Session::class.java)

        // when
        service.add(session)

        // then
        assertTrue(service.sessions.isNotEmpty())
    }

    @Test
    fun whenRegionIsSet_shouldCallApi() {
        // given
        val square = GeoSquare(0.0, 0.0, 0.0, 0.0)
        val apiSpy = spy<ApiService>(apiService)
        val service = SessionsInRegionDownloadService(apiSpy)

        // when
        service.setRegion(square)

        // then
        verify(apiSpy).getSessionsInRegion()
    }

    @Test
    fun whenRegionIsSet_apiCall_shouldContainRegionCoordinates() {
        // given
        val apiSpy = spy<ApiService>(apiService)
        val service = SessionsInRegionDownloadService(apiSpy)

        // when
        service.setRegion(testSquare)

        // then
        verify(apiSpy).getSessionsInRegion(
            north = testSquare.north,
            south = testSquare.south,
            east = testSquare.east,
            west = testSquare.west
        )
    }

    @Test
    fun whenRegionIsSet_shouldEnqueueApiCall() {
        // given
        val apiSpy = spy<ApiService>(apiService)
        val service = SessionsInRegionDownloadService(apiSpy)
        val callSpy = spy(
            apiSpy.getSessionsInRegion(
                north = testSquare.north,
                south = testSquare.south,
                east = testSquare.east,
                west = testSquare.west
            )
        )
        doReturn(callSpy).`when`(apiSpy).getSessionsInRegion(
            north = testSquare.north,
            south = testSquare.south,
            east = testSquare.east,
            west = testSquare.west
        )

        // when
        service.setRegion(testSquare)

        // then
        verify(callSpy).enqueue(any())
    }

    @Test
    fun whenOnApiResponse_isSuccessful_shouldClearSessionsList() {
        // given
        val session = mock(Session::class.java)
        val service = SessionsInRegionDownloadService(apiService)
        service.add(session)

        // when
        service.setRegion(testSquare)
        Thread.sleep(2000)

        // then
        assertTrue(service.sessions.isEmpty())
    }

    @Test
    fun whenOnApiResponse_isUnSuccessful_shouldNotClearSessionsList() {
        // given
        mockWebServer.dispatcher = unsuccessDispatcher
        val session = mock(Session::class.java)
        val service = SessionsInRegionDownloadService(apiService)
        service.add(session)

        // when
        service.setRegion(testSquare)
        Thread.sleep(2000)

        // then
        assertTrue(service.sessions.isNotEmpty())
    }

    @Ignore("Not ready")
    @Test
    fun whenOnApiResponse_isSuccessful_thereAreSessionsInRegion_shouldAddSessionToList() {
        // given
        val service = SessionsInRegionDownloadService(apiService)

        // when
        service.setRegion(testSquare)
        Thread.sleep(2000)

        // then
        assertTrue(sessionsSpy.isNotEmpty())
    }

    private fun mockSuccessfulCallWithJson(json: String): Call<SessionsInRegionResponse> {
        val gson = Gson().fromJson(json, SessionsInRegionResponse::class.java)
        val response = Response.success(gson)
        return mock<Call<SessionsInRegionResponse>> {
            on { enqueue(any()) } doAnswer {
                val callback = it.arguments[0] as Callback<SessionsInRegionResponse>
                callback.onResponse(mock, response)
            }
        }
    }

    private fun mockApiServiceWithCall(call: Call<SessionsInRegionResponse>): ApiService {
        return mock<ApiService> {
            on {
                getSessionsInRegion(
                    north = testSquare.north,
                    south = testSquare.south,
                    east = testSquare.east,
                    west = testSquare.west
                )
            } doReturn call
        }
    }

    private fun mockUnsuccessfulCall(): Call<SessionsInRegionResponse> {
        val response = Response.error<SessionsInRegionResponse>(500, "{}".toResponseBody())
        return mock<Call<SessionsInRegionResponse>> {
            on { enqueue(any()) } doAnswer {
                val callback = it.arguments[0] as Callback<SessionsInRegionResponse>
                callback.onResponse(mock, response)
            }
        }
    }
}
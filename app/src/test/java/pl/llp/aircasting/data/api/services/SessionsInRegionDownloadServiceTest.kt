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
    fun whenGivenCoordinates_shouldCallToApi(): Unit = runBlocking {
        // given
        val mockResponse = mockSuccessfulResponseWithJson("{}")
        val mockApiService = mockApiServiceWithRes(mockResponse)
        val service = SessionsInRegionDownloadService(mockApiService)

        // when
        service.getSessionsFromRegion(testSquare)

        // then
        verify(mockApiService).getSessionsInRegion(anyOrNull())
    }

    @Test
    fun whenGivenCoordinates_getJson_shouldConstructCallContainingGivenCoordinates(): Unit =
        runBlocking {
            // when
            val json = SessionsInRegionDownloadService.constructAndGetJsonWith(testSquare)

            // then
            assertTrue(
                json.contains(testSquare.east.toString()) &&
                        json.contains(testSquare.west.toString()) &&
                        json.contains(testSquare.north.toString()) &&
                        json.contains(testSquare.south.toString())
            )
        }

    @Test(expected = Test.None::class)
    fun getJson_shouldNotThrowJsonSyntaxException(): Unit = runBlocking {
        // when
        val json = SessionsInRegionDownloadService.constructAndGetJsonWith(testSquare)

        // then
        JsonParser.parseString(json)
    }

    @Test
    fun whenCallingApi_shouldCallJsonToStringConverter(): Unit = runBlocking {
        // given
        val mockCall = mockSuccessfulCallWithJson("{}")
        val mockApiService = mockApiServiceWithCall(mockCall)
        val service = SessionsInRegionDownloadService(mockApiService)

        // when
        service.getSessionsFromRegionToList(testSquare, mutableListOf())

        // then
        verify(mockCall).enqueue(anyOrNull())
    }

    @Test
    fun whenApiResponseIsSuccessful_shouldClearGivenList() {
        // given
        val mockCall = mockSuccessfulCallWithJson("{}")
        val mockApiService = mockApiServiceWithCall(mockCall)
        val service = SessionsInRegionDownloadService(mockApiService)
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)

        // when
        service.getSessionsFromRegionToList(testSquare, sessionsSpy)

        // then
        verify(sessionsSpy).clear()
    }

    @Test
    fun whenApiResponseIsUnSuccessful_shouldNotClearGivenList() {
        // given
        val mockCall = mockUnsuccessfulCall()
        val mockApiService = mockApiServiceWithCall(mockCall)
        val service = SessionsInRegionDownloadService(mockApiService)
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)

        // when
        service.getSessionsFromRegionToList(testSquare, sessionsSpy)

        // then
        verify(sessionsSpy, never()).clear()
    }


    @Test
    fun whenThereAreNoSessionsDownloaded_shouldNotAddToGivenList() {
        // given
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)
        val mockCall = mockSuccessfulCallWithJson("{}")
        val mockApiService = mockApiServiceWithCall(mockCall)
        val service = SessionsInRegionDownloadService(mockApiService)

        // when
        service.getSessionsFromRegionToList(testSquare, sessionsSpy)

        // then
        verify(sessionsSpy, never()).add(anyOrNull())
    }

    @Test
    fun whenThereAreNoSessionsDownloaded_shouldHaveGivenListEmpty() {
        // given
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)
        val mockCall = mockSuccessfulCallWithJson("{}")
        val mockApiService = mockApiServiceWithCall(mockCall)
        val service = SessionsInRegionDownloadService(mockApiService)

        // when
        service.getSessionsFromRegionToList(testSquare, sessionsSpy)

        // then
        assertTrue(sessionsSpy.isEmpty())
    }

    @Test
    fun whenThereAreSessionsDownloaded_shouldAddToGivenList() {
        // given
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)
        val mockCall = mockSuccessfulCallWithJson(testJson)
        val mockApiService = mockApiServiceWithCall(mockCall)
        val service = SessionsInRegionDownloadService(mockApiService)

        // when
        service.getSessionsFromRegionToList(testSquare, sessionsSpy)

        // then
        verify(sessionsSpy).add(anyOrNull())
    }

    @Test
    fun whenThereAreSessionsDownloaded_shouldHaveGivenListNonEmpty() {
        // given
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)
        val mockCall = mockSuccessfulCallWithJson(testJson)
        val mockApiService = mockApiServiceWithCall(mockCall)
        val service = SessionsInRegionDownloadService(mockApiService)

        // when
        service.getSessionsFromRegionToList(testSquare, sessionsSpy)

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
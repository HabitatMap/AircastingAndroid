package pl.llp.aircasting.data.api.services

import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Ignore
import org.junit.Test
import org.mockito.kotlin.*
import pl.llp.aircasting.data.api.responses.SessionInRegionResponse
import pl.llp.aircasting.data.api.responses.SessionsInRegionResponse
import pl.llp.aircasting.data.api.responses.search.SessionsInRegionsRes
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.utilities.StubData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionsInRegionDownloadRepositoryTest {
    private val testSquare = GeoSquare(1.0, 1.0, 1.0, 1.0)
    private val testJson = StubData.getJson("SessionsCracow.json")

    @Test
    fun whenGivenCoordinates_shouldCallToApi(): Unit = runBlocking {
        // given
        val mockResponse = mockSuccessfulResponseWithJson("{}")
        val mockApiService = mockApiServiceWithRes(mockResponse)
        val service = SessionsInRegionDownloadRepository(mockApiService)

        // when
        service.getSessionsFromRegion(testSquare)

        // then
        verify(mockApiService).getSessionsInRegion(anyOrNull())
    }

    @Test
    fun constructAndGetJsonWith_shouldConstructJsonContainingGivenCoordinates(): Unit =
        runBlocking {
            // given
            val json = SessionsInRegionDownloadRepository.constructAndGetJsonWith(testSquare)
            val jsonObject = JsonParser.parseString(json).asJsonObject

            // when
            val east = jsonObject.get("east").asJsonPrimitive.asDouble
            val west = jsonObject.get("west").asJsonPrimitive.asDouble
            val south = jsonObject.get("south").asJsonPrimitive.asDouble
            val north = jsonObject.get("north").asJsonPrimitive.asDouble

            // then
            assertEquals(testSquare.east, east)
            assertEquals(testSquare.west, west)
            assertEquals(testSquare.north, north)
            assertEquals(testSquare.south, south)
        }

    @Test
    fun constructAndGetJsonWith_shouldContain_currentStartAndEndTimeOfTheDay() {
        // given
        val currentDayTimeFrom = getStartOfDayEpoch()
        val currentDayTimeTo = getEndOfDayEpoch()

        val json = SessionsInRegionDownloadRepository.constructAndGetJsonWith(testSquare)
        val jsonObject = JsonParser.parseString(json).asJsonObject

        // when
        val timeFrom = jsonObject.getAsJsonPrimitive("time_from").asLong
        val timeTo = jsonObject.getAsJsonPrimitive("time_to").asLong

        // then
        assertEquals(currentDayTimeFrom, timeFrom)
        assertEquals(currentDayTimeTo, timeTo)
    }

    @Test(expected = Test.None::class)
    fun constructAndGetJsonWith_shouldNotThrowJsonSyntaxException(): Unit = runBlocking {
        // when
        val json = SessionsInRegionDownloadRepository.constructAndGetJsonWith(testSquare)

        // then
        JsonParser.parseString(json)
    }


    @Test
    fun whenCallingApi_shouldCallJsonToStringConverter(): Unit = runBlocking {
        // given
        val mockResponse = mockSuccessfulResponseWithJson("{}")
        val mockApiService = mockApiServiceWithRes(mockResponse)
        val service = SessionsInRegionDownloadRepository(mockApiService)

        // when
        service.getSessionsFromRegion(testSquare)

        // then
        verify(mockApiService).getSessionsInRegion(
            argThat {
                equals(SessionsInRegionDownloadRepository.constructAndGetJsonWith(testSquare))
            }
        )
    }

    @Ignore
    @Test
    fun whenApiResponseIsSuccessful_shouldClearGivenList(): Unit = runBlocking {
        // given
        val mockResponse = mockSuccessfulResponseWithJson("{}")
        val mockApiService = mockApiServiceWithRes(mockResponse)
        val service = SessionsInRegionDownloadRepository(mockApiService)
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)

        // when
        service.getSessionsFromRegion(testSquare)

        // then
        verify(sessionsSpy).clear()
    }

    @Ignore
    @Test
    fun whenApiResponseIsUnSuccessful_shouldNotClearGivenList(): Unit = runBlocking {
        // given
        val mockCall = mockUnsuccessfulCall()
        val mockResponse = mockSuccessfulResponseWithJson(testJson)
        val mockApiService = mockApiServiceWithRes(mockResponse)
        val service = SessionsInRegionDownloadRepository(mockApiService)
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)

        // when
        service.getSessionsFromRegion(testSquare)

        // then
        verify(sessionsSpy, never()).clear()
    }

    @Ignore
    @Test
    fun whenThereAreNoSessionsDownloaded_shouldNotAddToGivenList(): Unit = runBlocking {
        // given
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)
        val mockResponse = mockSuccessfulResponseWithJson("{}")
        val mockApiService = mockApiServiceWithRes(mockResponse)
        val service = SessionsInRegionDownloadRepository(mockApiService)

        // when
        service.getSessionsFromRegion(testSquare)

        // then
        verify(sessionsSpy, never()).add(anyOrNull())
    }

    @Ignore
    @Test
    fun whenThereAreNoSessionsDownloaded_shouldHaveGivenListEmpty(): Unit = runBlocking {
        // given
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)
        val mockResponse = mockSuccessfulResponseWithJson("{}")
        val mockApiService = mockApiServiceWithRes(mockResponse)
        val service = SessionsInRegionDownloadRepository(mockApiService)

        // when
        service.getSessionsFromRegion(testSquare)

        // then
        assertTrue(sessionsSpy.isEmpty())
    }

    @Ignore
    @Test
    fun whenThereAreSessionsDownloaded_shouldAddSessionsToGivenList(): Unit = runBlocking {
        // given
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)
        val mockResponse = mockSuccessfulResponseWithJson(testJson)
        val mockApiService = mockApiServiceWithRes(mockResponse)
        val service = SessionsInRegionDownloadRepository(mockApiService)

        // when
        service.getSessionsFromRegion(testSquare)

        // then
        verify(sessionsSpy).add(anyOrNull())
    }

    @Ignore
    @Test
    fun whenThereAreSessionsDownloaded_shouldHaveGivenListNonEmpty(): Unit = runBlocking {
        // given
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)
        val mockResponse = mockSuccessfulResponseWithJson(testJson)
        val mockApiService = mockApiServiceWithRes(mockResponse)
        val service = SessionsInRegionDownloadRepository(mockApiService)

        // when
        service.getSessionsFromRegion(testSquare)

        // then
        assertTrue(sessionsSpy.isNotEmpty())
    }

    @Ignore
    @Test
    fun whenThereAreSessionsDownloaded_shouldAddSessionsFromResponse(): Unit = runBlocking {
        // given
        val sessions = mutableListOf<Session>()
        val sessionsSpy = spy(sessions)
        val response = mockSuccessfulResponseWithJson(testJson)
        val mockApiService = mockApiServiceWithRes(response)
        val service = SessionsInRegionDownloadRepository(mockApiService)

        // when


    }



    private fun mockApiServiceWithRes(res: SessionsInRegionsRes): ApiService = runBlocking {
        return@runBlocking mock<ApiService> {
            onBlocking {
                getSessionsInRegion(
                    anyOrNull()
                )
            } doReturn res
        }
    }

    private fun mockSuccessfulResponseWithJson(json: String): SessionsInRegionsRes {
        return Gson().fromJson(json, SessionsInRegionsRes::class.java)
    }

    private fun mockUnsuccessfulCall(): Call<SessionsInRegionResponse> {
        val response = Response.error<SessionsInRegionResponse>(500, "{}".toResponseBody())
        return mock<Call<SessionsInRegionResponse>> {
            on { enqueue(anyOrNull()) } doAnswer {
                val callback = it.arguments[0] as Callback<SessionsInRegionResponse>
                callback.onResponse(mock, response)
            }
        }
    }

    private fun getStartOfDayEpoch(): Long {
        val secondsInDay = (60 * 60 * 24).toLong()
        val currentSecond = System.currentTimeMillis() / 1000
        return currentSecond - currentSecond % secondsInDay
    }

    private fun getEndOfDayEpoch(): Long {
        val startOfTheDayEpoch = getStartOfDayEpoch()
        val secondInDay = (60 * 60 * 24).toLong()
        return startOfTheDayEpoch + secondInDay - 1
    }

    private fun firstSessionFromResponse(response: SessionInRegionResponse): Session {
        return Session(
            response.uuid,
            null,
            null,
            Session.Type.FIXED,
            response.title,
            arrayListOf(),
            Session.Status.RECORDING,
            Date(Date.parse(response.start_time_local))
        )
    }
}
package pl.llp.aircasting.data.api.services

import okhttp3.mockwebserver.*
import org.junit.*
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.utilities.StubData
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import kotlin.test.assertTrue

class SessionsInRegionDownloadServiceTest {
    @get:Rule
    val mockWebServer = MockWebServer()
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val apiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    private val testJson = StubData.getJson("SessionsCracow.json")
    private val dispatcher = object : QueueDispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return if (request.path == "/api/fixed/active/sessions.json") {
                MockResponse()
                    .setBody(testJson)
                    .setResponseCode(200)
            } else MockResponse()
                .setResponseCode(500)
        }
    }

    @Before
    fun setup() {
        mockWebServer.dispatcher = dispatcher
    }

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
        val square = GeoSquare(1.0, 1.0, 1.0, 1.0)
        val apiSpy = spy<ApiService>(apiService)
        val service = SessionsInRegionDownloadService(apiSpy)

        // when
        service.setRegion(square)

        // then
        verify(apiSpy).getSessionsInRegion(
            north = square.north,
            south = square.south,
            east = square.east,
            west = square.west
        )
    }

    @Test
    fun whenRegionIsSet_shouldEnqueueApiCall() {
        // given
        val square = GeoSquare(1.0, 1.0, 1.0, 1.0)
        val apiSpy = spy<ApiService>(apiService)
        val service = SessionsInRegionDownloadService(apiSpy)
        val callSpy = spy(
            apiSpy.getSessionsInRegion(
                north = square.north,
                south = square.south,
                east = square.east,
                west = square.west
            )
        )
        doReturn(callSpy).`when`(apiSpy).getSessionsInRegion(
            north = square.north,
            south = square.south,
            east = square.east,
            west = square.west
        )

        // when
        service.setRegion(square)

        // then
        verify(callSpy).enqueue(any())
    }
}
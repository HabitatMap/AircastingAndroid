package pl.llp.aircasting.data.api.services

import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import pl.llp.aircasting.data.api.responses.SessionsInRegionResponse
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.utilities.StubData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
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

    private val testJson = StubData.getJson("Cracow.json")

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
        val callSpy = spy(apiSpy.getSessionsInRegion(
            north = square.north,
            south = square.south,
            east = square.east,
            west = square.west
        ))
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
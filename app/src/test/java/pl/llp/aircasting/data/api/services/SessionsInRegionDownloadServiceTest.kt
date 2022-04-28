package pl.llp.aircasting.data.api.services

import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.utilities.StubData
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionsInRegionDownloadServiceTest {
    @get:Rule
    val mockWebServer = MockWebServer()
    private val retrofit by lazy {
        Retrofit.Builder()
            // 1
            .baseUrl(mockWebServer.url("/"))
            // 2
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            // 3
            .addConverterFactory(GsonConverterFactory.create())
            // 4
            .build()
    }
    private val apiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    private val testJson = StubData.getJson("Cracow.json")

    @Test
    fun whenThereAreNoSessions_shouldReturnEmptyList() {
        val service = SessionsInRegionDownloadService()

        assertTrue(service.sessions.isEmpty())
    }

    @Test
    fun whenThereAreSessions_shouldReturnNonEmptyList() {
        val service = SessionsInRegionDownloadService()
        val session = Mockito.mock(Session::class.java)
        service.add(session)

        assertTrue(service.sessions.isNotEmpty())
    }
}
package pl.llp.aircasting.data.local.repositories

import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.model.Session
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import kotlin.test.assertTrue


class SearchedFixedSessionsRepositoryTest {
    private val repository = SearchedFixedSessionsRepository()
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

    @Test
    fun whenGivenCoordinatesPair_whereNoSessionsArePresent_shouldReturnEmptyList() {
        val lat = 0.0
        val lng = 0.0

        val result: List<Session> = repository.getSessionsInside(lat, lng)

        assertTrue(result.isEmpty())
    }

    @Test
    fun whenGivenCoordinatesPair_whereSessionsArePresent_shouldReturnNonEmptyList() {
        val lat = 1.0
        val lng = 1.0


    }
}
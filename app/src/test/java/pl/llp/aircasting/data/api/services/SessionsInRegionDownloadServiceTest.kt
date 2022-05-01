package pl.llp.aircasting.data.api.services

import okhttp3.mockwebserver.*
import org.junit.*
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.utilities.StubData
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.assertTrue

class SessionsInRegionDownloadServiceTest {
    private val testSquare = GeoSquare(1.0,1.0,1.0,1.0)
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
    private val successDispatcher = object : QueueDispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return MockResponse()
                    .setBody(testJson)
                    .setResponseCode(200)
        }
    }
    private val unsuccessDispatcher = object : QueueDispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return MockResponse()
                .setResponseCode(500)
        }
    }

    @Before
    fun setup() {
        mockWebServer.dispatcher = successDispatcher
    }

    @After
    fun cleanup() {
        mockWebServer.shutdown()
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
        assertTrue(service.sessions.isNotEmpty())
    }
}
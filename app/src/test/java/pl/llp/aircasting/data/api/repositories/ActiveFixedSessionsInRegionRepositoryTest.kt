package pl.llp.aircasting.data.api.repositories

import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.*
import pl.llp.aircasting.data.api.responses.search.SessionsInRegionsRes
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.util.Ozone
import pl.llp.aircasting.data.api.util.ParticulateMatter
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.util.ResponseHandler
import pl.llp.aircasting.util.Status
import pl.llp.aircasting.utilities.StubData
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ActiveFixedSessionsInRegionRepositoryTest {
    private val testSquare = GeoSquare(1.0, 1.0, 1.0, 1.0)
    private val sessionsInRegionResponse = StubData.getJson("SessionsCracow.json")
    private val streamOfGivenSessionResponse = StubData.getJson("StreamSensorNameHabitatMap.json")

    @Test
    fun whenGivenCoordinates_shouldCallToApi(): Unit = runBlocking {
        // given
        val mockResponse = mockGetSessionsInRegionResponseWithJson("{}")
        val mockApiService = mockGetSessionsInRegionCallWithResponse(mockResponse)
        val mockHandler = mock<ResponseHandler>()
        val service = ActiveFixedSessionsInRegionRepository(mockApiService, mockHandler)

        // when
        service.getSessionsFromRegion(testSquare, ParticulateMatter.AIRBEAM)

        // then
        verify(mockApiService).getSessionsInRegion(anyOrNull())
    }

    @Test
    fun constructAndGetJsonWith_shouldConstructJsonContainingGivenCoordinates(): Unit =
        runBlocking {
            // given
            val json = ActiveFixedSessionsInRegionRepository.constructAndGetJsonWith(
                testSquare,
                ParticulateMatter.AIRBEAM
            )
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
    fun constructAndGetJsonWith_shouldContainTimeParametersAs_currentStartAndEndTimeOfTheDay() {
        // given
        val currentDayTimeFrom = getStartOfDayEpoch()
        val currentDayTimeTo = getEndOfDayEpoch()

        val json = ActiveFixedSessionsInRegionRepository.constructAndGetJsonWith(
            testSquare,
            ParticulateMatter.AIRBEAM
        )
        val jsonObject = JsonParser.parseString(json).asJsonObject

        // when
        val timeFrom = jsonObject.getAsJsonPrimitive("time_from").asLong
        val timeTo = jsonObject.getAsJsonPrimitive("time_to").asLong

        // then
        assertEquals(currentDayTimeFrom, timeFrom)
        assertEquals(currentDayTimeTo, timeTo)
    }

    @Test
    fun constructAndGetJsonWith_shouldHaveRightQueryParameters_whenGivenParticulateMatterAirbeamSensor() {
        // given
        val expectedSensorName = "airbeam2-pm2.5"
        val expectedUnitSymbol = "µg/m³"
        val expectedMeasurementType = "Particulate Matter"
        val sensor = ParticulateMatter.AIRBEAM

        // when
        val result =
            ActiveFixedSessionsInRegionRepository.constructAndGetJsonWith(testSquare, sensor)

        // then
        val resultObject = JsonParser.parseString(result).asJsonObject
        assertTrue {
            resultObject.get("sensor_name").asString == expectedSensorName &&
                    resultObject.get("unit_symbol").asString == expectedUnitSymbol &&
                    resultObject.get("measurement_type").asString == expectedMeasurementType
        }
    }

    @Test
    fun constructAndGetJsonWith_shouldHaveRightQueryParameters_whenGivenParticulateMatterOpenAQSensor() {
        // given
        val expectedSensorName = "openaq-pm2.5"
        val expectedUnitSymbol = "µg/m³"
        val expectedMeasurementType = "Particulate Matter"
        val sensor = ParticulateMatter.OPEN_AQ

        // when
        val result =
            ActiveFixedSessionsInRegionRepository.constructAndGetJsonWith(testSquare, sensor)

        // then
        val resultObject = JsonParser.parseString(result).asJsonObject
        assertTrue {
            resultObject.get("sensor_name").asString == expectedSensorName &&
                    resultObject.get("unit_symbol").asString == expectedUnitSymbol &&
                    resultObject.get("measurement_type").asString == expectedMeasurementType
        }
    }

    @Test
    fun constructAndGetJsonWith_shouldHaveRightQueryParameters_whenGivenParticulateMatterPurpleAirSensor() {
        // given
        val expectedSensorName = "purpleair-pm2.5"
        val expectedUnitSymbol = "µg/m³"
        val expectedMeasurementType = "Particulate Matter"
        val sensor = ParticulateMatter.PURPLE_AIR

        // when
        val result =
            ActiveFixedSessionsInRegionRepository.constructAndGetJsonWith(testSquare, sensor)

        // then
        val resultObject = JsonParser.parseString(result).asJsonObject
        assertTrue {
            resultObject.get("sensor_name").asString == expectedSensorName &&
                    resultObject.get("unit_symbol").asString == expectedUnitSymbol &&
                    resultObject.get("measurement_type").asString == expectedMeasurementType
        }
    }

    @Test
    fun constructAndGetJsonWith_shouldHaveRightQueryParameters_whenGivenOzoneOpenAQSensor() {
        // given
        val expectedSensorName = "openaq-o3"
        val expectedUnitSymbol = "ppb"
        val expectedMeasurementType = "Ozone"
        val sensor = Ozone.OPEN_AQ

        // when
        val result =
            ActiveFixedSessionsInRegionRepository.constructAndGetJsonWith(testSquare, sensor)

        // then
        val resultObject = JsonParser.parseString(result).asJsonObject
        assertTrue {
            resultObject.get("sensor_name").asString == expectedSensorName &&
                    resultObject.get("unit_symbol").asString == expectedUnitSymbol &&
                    resultObject.get("measurement_type").asString == expectedMeasurementType
        }
    }

    @Test(expected = Test.None::class)
    fun constructAndGetJsonWith_shouldNotThrowJsonSyntaxException(): Unit = runBlocking {
        // when
        val json = ActiveFixedSessionsInRegionRepository.constructAndGetJsonWith(
            testSquare,
            ParticulateMatter.AIRBEAM
        )

        // then
        JsonParser.parseString(json)
    }

    @Test
    fun whenCallingApi_shouldCallJsonToStringConverter(): Unit = runBlocking {
        // given
        val mockResponse = mockGetSessionsInRegionResponseWithJson("{}")
        val mockApiService = mockGetSessionsInRegionCallWithResponse(mockResponse)
        val mockHandler = mock<ResponseHandler>()
        val service = ActiveFixedSessionsInRegionRepository(mockApiService, mockHandler)

        // when
        service.getSessionsFromRegion(testSquare, ParticulateMatter.AIRBEAM)

        // then
        verify(mockApiService).getSessionsInRegion(
            argThat {
                equals(
                    ActiveFixedSessionsInRegionRepository.constructAndGetJsonWith(
                        testSquare,
                        ParticulateMatter.AIRBEAM
                    )
                )
            }
        )
    }

    // Integration with ResponseHandler

    @Test
    fun whenApiResponseIsSuccessful_shouldReturnResourceWithSuccessStatus(): Unit = runBlocking {
        // given
        val mockResponse = mockGetSessionsInRegionResponseWithJson(sessionsInRegionResponse)
        val mockApiService = mockGetSessionsInRegionCallWithResponse(mockResponse)
        val mockHandler = mock<ResponseHandler> {
            whenever(mock.handleSuccess(any())).thenCallRealMethod()
        }
        whenever(mockHandler.handleSuccess(any())).thenCallRealMethod()
        val service = ActiveFixedSessionsInRegionRepository(mockApiService, mockHandler)
        val expected = Status.SUCCESS

        // when
        val result = service.getSessionsFromRegion(testSquare, ParticulateMatter.AIRBEAM)

        // then
        assertEquals(expected, result.status)
    }

    @Test
    fun whenApiResponseIsSuccessful_shouldReturnResourceWithNonNullData(): Unit = runBlocking {
        val mockResponse = mockGetSessionsInRegionResponseWithJson(sessionsInRegionResponse)
        val mockApiService = mockGetSessionsInRegionCallWithResponse(mockResponse)
        val mockHandler = mock<ResponseHandler> {
            whenever(mock.handleSuccess(any())).thenCallRealMethod()
        }
        whenever(mockHandler.handleSuccess(any())).thenCallRealMethod()
        val service = ActiveFixedSessionsInRegionRepository(mockApiService, mockHandler)

        // when
        val result = service.getSessionsFromRegion(testSquare, ParticulateMatter.AIRBEAM)

        // then
        assertNotNull(result.data)
    }

    private fun mockGetSessionsInRegionCallWithResponse(res: SessionsInRegionsRes): ApiService = runBlocking {
        return@runBlocking mock<ApiService> {
            onBlocking {
                getSessionsInRegion(
                    anyOrNull()
                )
            } doReturn res
        }
    }

    private fun mockGetStreamOfGivenSessionCallWithResponse(res: StreamOfGivenSessionResponse): ApiService = runBlocking {
        return@runBlocking mock<ApiService> {
            onBlocking {
                getStreamOfGivenSession(
                    anyOrNull(), anyOrNull()
                )
            } doReturn res
        }
    }

    private fun mockGetSessionsInRegionResponseWithJson(json: String): SessionsInRegionsRes {
        return Gson().fromJson(json, SessionsInRegionsRes::class.java)
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
}

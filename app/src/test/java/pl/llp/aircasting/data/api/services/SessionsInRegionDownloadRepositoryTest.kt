package pl.llp.aircasting.data.api.services

import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.*
import pl.llp.aircasting.data.api.repositories.GeoSquare
import pl.llp.aircasting.data.api.repositories.SessionsInRegionDownloadRepository
import pl.llp.aircasting.data.api.responses.search.SessionsInRegionsRes
import pl.llp.aircasting.data.api.util.Ozone
import pl.llp.aircasting.data.api.util.ParticulateMatter
import pl.llp.aircasting.util.Status
import pl.llp.aircasting.utilities.StubData
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SessionsInRegionDownloadRepositoryTest {
    private val testSquare = GeoSquare(1.0, 1.0, 1.0, 1.0)
    private val testJson = StubData.getJson("SessionsCracow.json")

    @Test
    fun whenGivenCoordinates_shouldCallToApi(): Unit = runBlocking {
        // given
        val mockResponse = mockSuccessfulResponseWithJson("{}")
        val mockApiService = mockApiServiceWithResponse(mockResponse)
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
    fun constructAndGetJsonWith_shouldContainTimeParametersAs_currentStartAndEndTimeOfTheDay() {
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

    @Test
    fun constructAndGetJsonWith_shouldHaveRightQueryParameters_whenGivenParticulateMatterAirbeamSensor() {
        // given
        val expectedSensorName = "airbeam2-pm2.5"
        val expectedUnitSymbol = "µg/m³"
        val expectedMeasurementType = "ParticulateMatter"
        val sensor = ParticulateMatter.AIRBEAM

        // when
        val result = SessionsInRegionDownloadRepository.constructAndGetJsonWith(testSquare, sensor)

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
        val expectedMeasurementType = "ParticulateMatter"
        val sensor = ParticulateMatter.OPEN_AQ

        // when
        val result = SessionsInRegionDownloadRepository.constructAndGetJsonWith(testSquare, sensor)

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
        val expectedMeasurementType = "ParticulateMatter"
        val sensor = ParticulateMatter.PURPLE_AIR

        // when
        val result = SessionsInRegionDownloadRepository.constructAndGetJsonWith(testSquare, sensor)

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
        val result = SessionsInRegionDownloadRepository.constructAndGetJsonWith(testSquare, sensor)

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
        val json = SessionsInRegionDownloadRepository.constructAndGetJsonWith(testSquare)

        // then
        JsonParser.parseString(json)
    }

    @Test
    fun whenCallingApi_shouldCallJsonToStringConverter(): Unit = runBlocking {
        // given
        val mockResponse = mockSuccessfulResponseWithJson("{}")
        val mockApiService = mockApiServiceWithResponse(mockResponse)
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

    // Integration with ResponseHandler

    @Test
    fun whenApiResponseIsSuccessful_shouldReturnResourceWithSuccessStatus(): Unit = runBlocking {
        // given
        val mockResponse = mockSuccessfulResponseWithJson(testJson)
        val mockApiService = mockApiServiceWithResponse(mockResponse)
        val service = SessionsInRegionDownloadRepository(mockApiService)
        val expected = Status.SUCCESS

        // when
        val result = service.getSessionsFromRegion(testSquare)

        // then
        assertEquals(expected, result.status)
    }

    @Test
    fun whenApiResponseIsSuccessful_shouldReturnResourceWithNonNullData(): Unit = runBlocking {
        val mockResponse = mockSuccessfulResponseWithJson(testJson)
        val mockApiService = mockApiServiceWithResponse(mockResponse)
        val service = SessionsInRegionDownloadRepository(mockApiService)

        // when
        val result = service.getSessionsFromRegion(testSquare)

        // then
        assertNotNull(result.data)
    }

    private fun mockApiServiceWithResponse(res: SessionsInRegionsRes): ApiService = runBlocking {
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

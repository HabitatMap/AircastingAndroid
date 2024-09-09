package pl.llp.aircasting.data.api.repository

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.mockito.kotlin.*
import pl.llp.aircasting.data.api.response.StreamOfGivenSessionResponse
import pl.llp.aircasting.data.api.response.search.SessionsInRegionsResponse
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.util.NitrogenDioxide
import pl.llp.aircasting.data.api.util.Ozone
import pl.llp.aircasting.data.api.util.ParticulateMatter
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.util.Resource
import pl.llp.aircasting.util.ResponseHandler
import pl.llp.aircasting.util.Status
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.util.extensions.getEndOfTodayEpoch
import pl.llp.aircasting.util.extensions.getStartOfTodayEpochFromYearAgo
import pl.llp.aircasting.utilities.StubData
import pl.llp.aircasting.utilities.StubData.mockGetSessionsInRegionResponseWithJson
import pl.llp.aircasting.utilities.StubData.mockGetStreamOfGivenSessionResponseWithJson
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ActiveFixedSessionsInRegionRepositoryTest {
    private val testSquare = GeoSquare(1.0, 1.0, 1.0, 1.0)
    private val sessionsInRegionResponse = StubData.getJson("SessionsCracow.json")
    private val streamOfGivenSessionResponse = StubData.getJson("StreamSensorNameHabitatMap.json")

    @Ignore("Repository calls getSessionsInRegion 2 times to combine AB3 and AB2 sessions. This will need to be transferred to ViewModel")
    @Test
    fun whenGivenCoordinates_shouldCallToApi(): Unit = runBlocking {
        // given
        val mockResponse = mockGetSessionsInRegionResponseWithJson("{}")
        val mockApiServiceFactory = mockGetSessionsInRegionCallWithResponse(mockResponse)
        val mockHandler = mock<ResponseHandler>()
        val repository = ActiveFixedSessionsInRegionRepository(mockApiServiceFactory, mockHandler)

        // when
        repository.getSessionsFromRegion(testSquare, ParticulateMatter.AIRBEAM)

        // then
        verify(mockApiServiceFactory).getSessionsInRegion(anyOrNull())
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
        val currentDayTimeFrom = calendar().getStartOfTodayEpochFromYearAgo()
        val currentDayTimeTo = calendar().getEndOfTodayEpoch()

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
    fun constructAndGetJsonWith_shouldHaveRightQueryParameters_whenGivenParticulateMatterGovernmentSensor() {
        // given
        val expectedSensorName = "government-pm2.5"
        val expectedUnitSymbol = "µg/m³"
        val expectedMeasurementType = "Particulate Matter"
        val sensor = ParticulateMatter.GOVERNMENT

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
    fun constructAndGetJsonWith_shouldHaveRightQueryParameters_whenGivenOzoneGovernmentSensor() {
        // given
        val expectedSensorName = "government-ozone"
        val expectedUnitSymbol = "ppb"
        val expectedMeasurementType = "Ozone"
        val sensor = Ozone.GOVERNMENT

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
    fun constructAndGetJsonWith_shouldHaveRightQueryParameters_whenGivenNitrogenDioxideGovernmentSensor() {
        // given
        val expectedSensorName = "government-no2"
        val expectedUnitSymbol = "ppb"
        val expectedMeasurementType = "Nitrogen Dioxide"
        val sensor = NitrogenDioxide.GOVERNMENT

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
        val mockApiServiceFactory = mockGetSessionsInRegionCallWithResponse(mockResponse)
        val mockHandler = mock<ResponseHandler>()
        val repository = ActiveFixedSessionsInRegionRepository(mockApiServiceFactory, mockHandler)

        // when
        repository.getSessionsFromRegion(testSquare, ParticulateMatter.AIRBEAM)

        // then
        verify(mockApiServiceFactory).getSessionsInRegion(
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

    @Test
    fun whenGettingStreamOfGivenSession_shouldCallApi(): Unit = runBlocking {
        // given
        val mockResponse = mockGetStreamOfGivenSessionResponseWithJson(streamOfGivenSessionResponse)
        val mockApiServiceFactory = mockGetStreamOfGivenSessionCallWithResponse(mockResponse)
        val mockHandler = mock<ResponseHandler> {
            whenever(mock.handleSuccess(any())).thenCallRealMethod()
        }
        val repository = ActiveFixedSessionsInRegionRepository(mockApiServiceFactory, mockHandler)

        // when
        repository.getStreamOfGivenSession(1758913L, "AirBeam3-PM2.5")

        // then
        verify(mockApiServiceFactory).getStreamOfGivenSession(anyOrNull(), anyOrNull(), eq(1))
    }

    @Test
    fun whenGettingStreamOfGivenSession_shouldReturnResourceWithDataWithCorrespondingToResponse(): Unit =
        runBlocking {
            // given
            val mockResponse =
                mockGetStreamOfGivenSessionResponseWithJson(streamOfGivenSessionResponse)
            val mockApiServiceFactory = mockGetStreamOfGivenSessionCallWithResponse(mockResponse)
            val mockHandler = mock<ResponseHandler> {
                whenever(mock.handleSuccess(any())).thenCallRealMethod()
            }
            val repository = ActiveFixedSessionsInRegionRepository(mockApiServiceFactory, mockHandler)

            // when
            val response: Resource<StreamOfGivenSessionResponse> =
                repository.getStreamOfGivenSession(1758913L, "AirBeam3-PM2.5")

            // then
            assertTrue {
                response.data == mockResponse
            }
        }

    @Test
    fun whenGettingStreamOfGivenSession_shouldReturnResponseWithSessionIdAndSensorNameSameAsInCall(): Unit =
        runBlocking {
            // given
            val mockResponse =
                mockGetStreamOfGivenSessionResponseWithJson(streamOfGivenSessionResponse)
            val mockApiServiceFactory = mockGetStreamOfGivenSessionCallWithResponse(mockResponse)
            val mockHandler = mock<ResponseHandler> {
                whenever(mock.handleSuccess(any())).thenCallRealMethod()
            }
            val repository = ActiveFixedSessionsInRegionRepository(mockApiServiceFactory, mockHandler)
            val expectedId = 1758913L
            val expectedSensorName = "AirBeam3-PM2.5"

            // when
            repository.getStreamOfGivenSession(1758913L, "AirBeam3-PM2.5")

            // then
            verify(mockApiServiceFactory).getStreamOfGivenSession(
                eq(expectedId),
                eq(expectedSensorName),
                eq(1)
            )
        }

    @Test
    fun whenGettingStreamOfGivenSession_shouldConstructCallWithGivenSessionIdAndSensorName(): Unit =
        runBlocking {
            // given
            val mockResponse =
                mockGetStreamOfGivenSessionResponseWithJson(streamOfGivenSessionResponse)
            val mockApiServiceFactory = mockGetStreamOfGivenSessionCallWithResponse(mockResponse)
            val mockHandler = mock<ResponseHandler> {
                whenever(mock.handleSuccess(any())).thenCallRealMethod()
            }
            val repository = ActiveFixedSessionsInRegionRepository(mockApiServiceFactory, mockHandler)
            val expectedId = 123L
            val expectedSensorName = "Ozone"
            val expectedMeasurementLimit = 1

            // when
            repository.getStreamOfGivenSession(123L, "Ozone")

            // then
            verify(mockApiServiceFactory).getStreamOfGivenSession(
                eq(expectedId),
                eq(expectedSensorName),
                eq(expectedMeasurementLimit)
            )
        }

    // Integration with ResponseHandler

    @Test
    fun whenApiResponseIsSuccessful_shouldReturnResourceWithSuccessStatus(): Unit = runBlocking {
        // given
        val mockResponse = mockGetSessionsInRegionResponseWithJson(sessionsInRegionResponse)
        val mockApiServiceFactory = mockGetSessionsInRegionCallWithResponse(mockResponse)
        val mockHandler = mock<ResponseHandler> {
            whenever(mock.handleSuccess(any())).thenCallRealMethod()
        }
        whenever(mockHandler.handleSuccess(any())).thenCallRealMethod()
        val repository = ActiveFixedSessionsInRegionRepository(mockApiServiceFactory, mockHandler)
        val expected = Status.SUCCESS

        // when
        val result = repository.getSessionsFromRegion(testSquare, ParticulateMatter.AIRBEAM)

        // then
        assertEquals(expected, result.status)
    }

    @Test
    fun whenApiResponseIsSuccessful_shouldReturnResourceWithNonNullData(): Unit = runBlocking {
        // given
        val mockResponse = mockGetSessionsInRegionResponseWithJson(sessionsInRegionResponse)
        val mockApiServiceFactory = mockGetSessionsInRegionCallWithResponse(mockResponse)
        val mockHandler = mock<ResponseHandler> {
            whenever(mock.handleSuccess(any())).thenCallRealMethod()
        }
        whenever(mockHandler.handleSuccess(any())).thenCallRealMethod()
        val repository = ActiveFixedSessionsInRegionRepository(mockApiServiceFactory, mockHandler)

        // when
        val result = repository.getSessionsFromRegion(testSquare, ParticulateMatter.AIRBEAM)

        // then
        assertNotNull(result.data)
    }

    @Test
    fun whenGettingStreamOfGivenSessionIsSuccessful_shouldReturnResourceWithSuccessStatus(): Unit =
        runBlocking {
            // given
            val mockResponse =
                mockGetStreamOfGivenSessionResponseWithJson(streamOfGivenSessionResponse)
            val mockApiServiceFactory = mockGetStreamOfGivenSessionCallWithResponse(mockResponse)
            val mockHandler = mock<ResponseHandler> {
                whenever(mock.handleSuccess(any())).thenCallRealMethod()
            }
            val repository = ActiveFixedSessionsInRegionRepository(mockApiServiceFactory, mockHandler)
            val expected = Status.SUCCESS

            // when
            val result = repository.getStreamOfGivenSession(123L, "Ozone")

            // then
            assertEquals(expected, result.status)
        }

    @Test
    fun whenGettingStreamOfGivenSessionIsSuccessful_shouldReturnResourceWithNonNullData(): Unit =
        runBlocking {
            // given
            val mockResponse =
                mockGetStreamOfGivenSessionResponseWithJson(streamOfGivenSessionResponse)
            val mockApiServiceFactory = mockGetStreamOfGivenSessionCallWithResponse(mockResponse)
            val mockHandler = mock<ResponseHandler> {
                whenever(mock.handleSuccess(any())).thenCallRealMethod()
            }
            val repository = ActiveFixedSessionsInRegionRepository(mockApiServiceFactory, mockHandler)

            // when
            val result = repository.getStreamOfGivenSession(123L, "Ozone")

            // then
            assertNotNull(result.data)
        }

    private fun mockGetSessionsInRegionCallWithResponse(res: SessionsInRegionsResponse): ApiService =
        runBlocking {
            return@runBlocking mock<ApiService> {
                onBlocking {
                    getSessionsInRegion(
                        anyOrNull()
                    )
                } doReturn res
            }
        }

    private fun mockGetStreamOfGivenSessionCallWithResponse(res: StreamOfGivenSessionResponse): ApiService =
        runBlocking {
            return@runBlocking mock<ApiService> {
                onBlocking {
                    getStreamOfGivenSession(
                        anyOrNull(), anyOrNull(), anyOrNull()
                    )
                } doReturn res
            }
        }
}

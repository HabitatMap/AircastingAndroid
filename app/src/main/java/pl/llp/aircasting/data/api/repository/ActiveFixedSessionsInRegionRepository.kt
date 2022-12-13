package pl.llp.aircasting.data.api.repository

import pl.llp.aircasting.data.api.response.StreamOfGivenSessionResponse
import pl.llp.aircasting.data.api.response.search.SessionsInRegionsResponse
import pl.llp.aircasting.data.api.response.search.session.details.SessionWithStreamsAndMeasurementsResponse
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.util.Constants
import pl.llp.aircasting.data.api.util.ParticulateMatter
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.api.util.StringConstants
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.util.Resource
import pl.llp.aircasting.util.ResponseHandler
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.util.extensions.getEndOfTodayEpoch
import pl.llp.aircasting.util.extensions.getStartOfTodayEpochFromYearAgo
import javax.inject.Inject

class ActiveFixedSessionsInRegionRepository @Inject constructor(
    apiServiceFactory: ApiServiceFactory,
    private val responseHandler: ResponseHandler
) {
    private val apiService: ApiService = apiServiceFactory.get(emptyList())
    companion object {
        fun constructAndGetJsonWith(
            square: GeoSquare,
            sensorInfo: SensorInformation
        ): String {
            return "{\"time_from\":\"${calendar().getStartOfTodayEpochFromYearAgo()}\"," +
                    "\"time_to\":\"${calendar().getEndOfTodayEpoch()}\"," +
                    "\"tags\":\"\"," +
                    "\"usernames\":\"\"," +
                    "\"west\":${square.west}," +
                    "\"east\":${square.east}," +
                    "\"south\":${square.south}," +
                    "\"north\":${square.north}," +
                    "\"sensor_name\":\"${sensorInfo.getSensorName()}\"," +
                    "\"unit_symbol\":\"${sensorInfo.getUnitSymbol()}\"," +
                    "\"measurement_type\":\"${sensorInfo.getMeasurementType()}\"}"
        }
    }

    suspend fun getSessionsFromRegion(
        square: GeoSquare,
        sensorInfo: SensorInformation
    ): Resource<SessionsInRegionsResponse> {
        return try {
            val response =
                /* This is a temporary workaround, as AB3 sensorInfo has not been provided here
                * The class should not care about sensor names, they should be provided to it from the ViewModel
                *  */
                if (sensorIsAirBeam(sensorInfo)) {
                    val ab2 =
                        apiService.getSessionsInRegion(constructAndGetJsonWith(square, sensorInfo))
                    val ab3 = apiService.getSessionsInRegion(
                        constructAndGetJsonWith(
                            square,
                            ParticulateMatter.AIRBEAM3
                        )
                    )
                    combineResponses(ab2, ab3)
                } else
                    apiService.getSessionsInRegion(constructAndGetJsonWith(square, sensorInfo))
            responseHandler.handleSuccess(response)
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }

    private fun combineResponses(
        ab2: SessionsInRegionsResponse,
        ab3: SessionsInRegionsResponse
    ) =
        SessionsInRegionsResponse(
            ab2.fetchableSessionsCount + ab3.fetchableSessionsCount,
            ab2.sessions + ab3.sessions
        )

    private fun sensorIsAirBeam(sensorInfo: SensorInformation) =
        sensorInfo.getSensorName().contains(StringConstants.airbeam, true)

    suspend fun getStreamOfGivenSession(
        sessionId: Long,
        sensorName: String,
        measurementLimit: Int = 1
    ): Resource<StreamOfGivenSessionResponse> {
        return try {
            val response =
                apiService.getStreamOfGivenSession(sessionId, sensorName, measurementLimit)
            responseHandler.handleSuccess(response)
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }

    suspend fun getSessionWithStreamsAndMeasurements(
        sessionId: Long,
        measurementLimit: Int = Constants.MEASUREMENTS_IN_HOUR * 24
    ): Resource<SessionWithStreamsAndMeasurementsResponse> {
        return try {
            val response =
                apiService.getSessionWithStreamsAndMeasurements(sessionId, measurementLimit)
            responseHandler.handleSuccess(response)
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }
}
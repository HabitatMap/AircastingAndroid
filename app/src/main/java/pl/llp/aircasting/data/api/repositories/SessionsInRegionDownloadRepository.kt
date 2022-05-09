package pl.llp.aircasting.data.api.repositories

import pl.llp.aircasting.data.api.responses.search.SessionsInRegionsRes
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.util.ParticulateMatter
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.util.Resource
import pl.llp.aircasting.util.ResponseHandler
import java.lang.Exception

class SessionsInRegionDownloadRepository(
    private val apiService: ApiService,
    private val responseHandler: ResponseHandler = ResponseHandler()
) {
    companion object {
        fun constructAndGetJsonWith(square: GeoSquare, sensorInfo: SensorInformation = ParticulateMatter.AIRBEAM): String {
            return "{\"time_from\":\"${getStartOfDayEpoch()}\"," +
                    "\"time_to\":\"${getEndOfDayEpoch()}\"," +
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

    suspend fun getSessionsFromRegion(square: GeoSquare) {
        val sessionsInRegionsRes = apiService.getSessionsInRegion(
            constructAndGetJsonWith(square)
        )
    }
}

data class GeoSquare(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double
)
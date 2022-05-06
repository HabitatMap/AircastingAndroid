package pl.llp.aircasting.data.api.services

class SessionsInRegionDownloadRepository(private val apiService: ApiService) {
    companion object {
        fun constructAndGetJsonWith(square: GeoSquare): String {
            return "{\"time_from\":\"${getStartOfDayEpoch()}\"," +
                    "\"time_to\":\"${getEndOfDayEpoch()}\"," +
                    "\"tags\":\"\"," +
                    "\"usernames\":\"\"," +
                    "\"west\":${square.west}," +
                    "\"east\":${square.east}," +
                    "\"south\":${square.south}," +
                    "\"north\":${square.north}," +
                    "\"sensor_name\":\"airbeam2-pm2.5\"," +
                    "\"unit_symbol\":\"µg/m³\"," +
                    "\"measurement_type\":\"ParticulateMatter\"}"
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
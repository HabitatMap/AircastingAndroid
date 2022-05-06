package pl.llp.aircasting.data.api.services

class SessionsInRegionDownloadService(private val apiService: ApiService) {
    companion object {
        fun constructAndGetJsonWith(square: GeoSquare): String {
            return "{\"time_from\":\"1531008000\"" +
                    ",\"time_to\":\"1562630399\"," +
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
package pl.llp.aircasting.data.api.repositories

import pl.llp.aircasting.data.api.services.ApiService
import javax.inject.Inject

class SearchFollowRepository @Inject constructor(private val apiService: ApiService) {
    suspend fun getSessionsInRegion(
        north: Double,
        south: Double,
        east: Double,
        west: Double,
        time_from: Int,
        time_to: Int,
        measurement_type: String,
        sensor_name: String,
        unit_symbol: String,
        tags: String,
        usernames: String
    ) = apiService.getSessionsInRegion(
        north,
        south,
        east,
        west,
        time_from,
        time_to,
        measurement_type,
        sensor_name,
        unit_symbol,
        tags,
        usernames
    )
}
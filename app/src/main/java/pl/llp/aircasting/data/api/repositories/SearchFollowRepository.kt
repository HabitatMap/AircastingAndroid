package pl.llp.aircasting.data.api.repositories

import pl.llp.aircasting.data.api.services.ApiService
import javax.inject.Inject

class SearchFollowRepository @Inject constructor(private val apiService: ApiService) {
    suspend fun getSessionsInRegion(query: String) = apiService.getSessionsInRegion(query)
}
package pl.llp.aircasting.data.api.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.llp.aircasting.data.api.params.CreateThresholdAlertBody
import pl.llp.aircasting.data.api.params.CreateThresholdAlertData
import pl.llp.aircasting.data.api.params.ThresholdAlertResponse
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

interface ThresholdAlertRepository {
    fun activeAlerts(): Flow<List<ThresholdAlertResponse>>
    suspend fun create(alert: CreateThresholdAlertData): Result<Int>
    suspend fun delete(id: Int): Result<Unit>
}

class ThresholdAlertRepositoryDefault @Inject constructor(
    apiServiceFactory: ApiServiceFactory,
    settings: Settings
) : ThresholdAlertRepository {
    private val apiService = apiServiceFactory.get(settings.getAuthToken() ?: "")

    override fun activeAlerts(): Flow<List<ThresholdAlertResponse>> = flow {
        emit(apiService.getThresholdAlerts())
    }

    override suspend fun create(alert: CreateThresholdAlertData): Result<Int> = runCatching {
        apiService.createThresholdAlert(CreateThresholdAlertBody(alert)).id
    }

    override suspend fun delete(id: Int): Result<Unit> = runCatching {
        apiService.deleteThresholdAlert(id)
    }
}
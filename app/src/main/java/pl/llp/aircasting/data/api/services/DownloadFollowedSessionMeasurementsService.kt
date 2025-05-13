package pl.llp.aircasting.data.api.services


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.OperationStatus
import javax.inject.Inject

@UserSessionScope
class DownloadFollowedSessionMeasurementsService @Inject constructor(
    private val sessionsRepository: SessionsRepository,
    private val downloadMeasurementsService: DownloadMeasurementsService,
) {
    private val _downloadStatus: MutableStateFlow<OperationStatus> = MutableStateFlow(OperationStatus.Idle)
    val downloadStatus get(): StateFlow<OperationStatus> = _downloadStatus

    suspend fun downloadMeasurements() {
        _downloadStatus.emit(OperationStatus.InProgress)

        val dbSessions = sessionsRepository.followedSessions()
        dbSessions.forEach { dbSession ->
            downloadMeasurements(dbSession.uuid)
        }
        _downloadStatus.emit(OperationStatus.Idle)
    }

    private suspend fun downloadMeasurements(uuid: String) {
        downloadMeasurementsService.downloadMeasurements(uuid)
    }
}
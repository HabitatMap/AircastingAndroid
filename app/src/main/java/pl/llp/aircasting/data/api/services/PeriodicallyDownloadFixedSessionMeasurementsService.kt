package pl.llp.aircasting.data.api.services


import kotlinx.coroutines.*
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.di.modules.IoCoroutineScope
import javax.inject.Inject

@UserSessionScope
class PeriodicallyDownloadFixedSessionMeasurementsService @Inject constructor(
    private val sessionsRepository: SessionsRepository,
    private val downloadMeasurementsService: DownloadMeasurementsService,
    @IoCoroutineScope private val coroutineScope: CoroutineScope,
) {
    private var downloadJob: Job? = null
    private var paused = false

    private val POLL_INTERVAL = 60 * 1000L // 1 minute

    fun start() {
        downloadJob = coroutineScope.downloadMeasurementsPeriodically()
    }

    fun stop() {
        downloadJob?.cancel()
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
    }

    private fun CoroutineScope.downloadMeasurementsPeriodically() = launch {
        while (isActive) {
            if (!paused) {
                downloadMeasurements()
            }
            delay(POLL_INTERVAL)
        }
    }

    private suspend fun downloadMeasurements() {
        val dbSessions = sessionsRepository.fixedSessions()
        dbSessions.forEach { dbSession ->
            val session = Session(dbSession)
            downloadMeasurements(dbSession.id, session)
        }
    }

    private suspend fun downloadMeasurements(sessionId: Long, session: Session) {
        downloadMeasurementsService.downloadMeasurementsForFixed(session, sessionId)
    }
}
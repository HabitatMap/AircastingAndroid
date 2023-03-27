package pl.llp.aircasting.data.api.services


import kotlinx.coroutines.*
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.exceptions.ErrorHandler

class PeriodicallyDownloadFixedSessionMeasurementsService(
    private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository = SessionsRepository(),
    private val downloadMeasurementsService: DownloadMeasurementsService = DownloadMeasurementsService(
        apiService,
        errorHandler
    ),
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
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
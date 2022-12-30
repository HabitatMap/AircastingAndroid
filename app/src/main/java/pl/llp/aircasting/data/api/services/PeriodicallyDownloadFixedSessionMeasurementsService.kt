package pl.llp.aircasting.data.api.services

import kotlinx.coroutines.*
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.exceptions.ErrorHandler


class PeriodicallyDownloadFixedSessionMeasurementsService(
    private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository = SessionsRepository(),
    private val downloadMeasurementsService: DownloadMeasurementsService = DownloadMeasurementsService(apiService, errorHandler),
) {
    private val thread: DownloadThread = DownloadThread()

    fun start() {
        thread.start()
    }

    fun stop() {
        thread.cancel()
    }

    fun pause() {
        thread.paused = true
    }

    fun resume() {
        thread.paused = false
    }

    // TODO: Convert to coroutine
    private inner class DownloadThread : Thread() {
        private val POLL_INTERVAL = 60 * 1000L // 1 minute
        var paused = false
        private var call: Job? = null

        override fun run() {
            try {
                while (!isInterrupted) {
                    downloadMeasurements()
                    sleep(POLL_INTERVAL)

                    while (paused) {
                        sleep(1000)
                    }
                }
            } catch (e: InterruptedException) {
                return
            }
        }

        fun cancel() {
            interrupt()
            call?.cancel()
        }

        private fun downloadMeasurements() {
            val dbSessions = sessionsRepository.fixedSessions()
            dbSessions.forEach { dbSession ->
                val session = Session(dbSession)
                downloadMeasurements(dbSession.id, session)
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun downloadMeasurements(sessionId: Long, session: Session) {
            call = GlobalScope.launch {
                downloadMeasurementsService.downloadMeasurementsForFixed(session, sessionId)
            }
        }
    }
}

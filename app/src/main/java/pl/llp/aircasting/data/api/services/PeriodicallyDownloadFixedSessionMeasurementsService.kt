package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.local.repositories.SessionsRepository
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.api.responses.SessionWithMeasurementsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call


class PeriodicallyDownloadFixedSessionMeasurementsService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    private val sessionsRepository = SessionsRepository()
    private val thread = DownloadThread()
    private val downloadMeasurementsService = DownloadMeasurementsService(apiService, errorHandler)

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

    // TODO: consider using WorkManager
    // https://developer.android.com/topic/libraries/architecture/workmanager/basics
    private inner class DownloadThread() : Thread() {
        private val POLL_INTERVAL = 60 * 1000L // 1 minute
        var paused = false
        private var call: Call<SessionWithMeasurementsResponse>? = null


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

        private fun downloadMeasurements(sessionId: Long, session: Session) {
            GlobalScope.launch(Dispatchers.IO) {
                call =
                    downloadMeasurementsService.enqueueDownloadingMeasurementsForFixed(sessionId, session)
            }
        }
    }
}

package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.responses.SessionWithMeasurementsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import java.util.*


class FixedSessionDownloadMeasurementsService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    private val sessionsRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val measurementsRepository = MeasurementsRepository()
    private val thread = DownloadThread()

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
        private var callback: DownloadMeasurementsCallback? = null


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
            callback?.callCanceled?.set(true)
        }

        private fun downloadMeasurements() {
            val dbSessions = sessionsRepository.fixedSessions()
            dbSessions.forEach { dbSession ->
                val session = Session(dbSession)
                downloadMeasurements(dbSession.id, session)
            }
        }

        private suspend fun lastMeasurementTime(sessionId: Long, session: Session): Date {
            var lastMeasurementTime: Date? = null
            val query = GlobalScope.async(Dispatchers.IO) {
                lastMeasurementTime = measurementsRepository.lastMeasurementTime(sessionId)
            }
            query.await()

            return LastMeasurementSyncCalculator.calculate(session.endTime, lastMeasurementTime)
        }

        private fun downloadMeasurements(sessionId: Long, session: Session) {
            callback =  DownloadMeasurementsCallback(
                sessionId, session, sessionsRepository, measurementStreamsRepository,
                measurementsRepository, errorHandler, null
            )
            GlobalScope.launch(Dispatchers.Main) {
                val lastMeasurementSyncTime = lastMeasurementTime(sessionId, session)
                val lastMeasurementSyncTimeString =
                    DateConverter.toDateString(lastMeasurementSyncTime)
                call =
                    apiService.downloadMeasurements(session.uuid, lastMeasurementSyncTimeString)

                call?.enqueue(callback)
            }
        }
    }
}

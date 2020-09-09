package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.DownloadMeasurementsError
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.networking.responses.SessionStreamWithMeasurementsResponse
import io.lunarlogic.aircasting.networking.responses.SessionWithMeasurementsResponse
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class DownloadMeasurementsService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    private val sessionsRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val measurementsRepository = MeasurementsRepository()
    private val thread = DownloadThread()

    fun start() {
        thread.start()
    }

    // TODO: consider using WorkManager
    // https://developer.android.com/topic/libraries/architecture/workmanager/basics
    private inner class DownloadThread(): Thread() {
        private val POLL_INTERVAL = 60 * 1000L // 1 minute

        override fun run() {
            while (true) {
                downloadMeasurements()
                sleep(POLL_INTERVAL)
            }
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
            GlobalScope.launch(Dispatchers.Main) {
                val lastMeasurementSyncTime = lastMeasurementTime(sessionId, session)
                val lastMeasurementSyncTimeString =
                    DateConverter.toDateString(lastMeasurementSyncTime)
                val call = apiService.downloadMeasurements(session.uuid, lastMeasurementSyncTimeString)

                call.enqueue(DownloadCallback(sessionId, session))
            }
        }

        private inner class DownloadCallback(private val sessionId: Long, private val session: Session): Callback<SessionWithMeasurementsResponse> {
            override fun onResponse(
                call: Call<SessionWithMeasurementsResponse>,
                response: Response<SessionWithMeasurementsResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.streams?.let { streams ->
                        DatabaseProvider.runQuery {
                            val streamResponses = streams.values
                            streamResponses.forEach { streamResponse ->
                                saveStreamData(streamResponse)
                            }
                        }
                    }
                } else {
                    errorHandler.handleAndDisplay(DownloadMeasurementsError())
                }
            }

            override fun onFailure(
                call: Call<SessionWithMeasurementsResponse>,
                t: Throwable
            ) {
                errorHandler.handleAndDisplay(DownloadMeasurementsError(t))
            }

            private fun saveStreamData(streamResponse: SessionStreamWithMeasurementsResponse) {
                val stream = MeasurementStream(streamResponse)
                val streamId = measurementStreamsRepository.getIdOrInsert(
                    sessionId,
                    stream
                )
                streamResponse.measurements.forEach { measurementResponse ->
                    val measurement = Measurement(measurementResponse)
                    measurementsRepository.insert(
                        streamId,
                        sessionId,
                        measurement
                    )
                    sessionsRepository.update(session)
                }
            }
        }
    }
}

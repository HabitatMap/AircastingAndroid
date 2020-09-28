package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.sensor.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DownloadMeasurementsService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    private val sessionsRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val measurementsRepository = MeasurementsRepository()

    fun downloadMeasurements(session: Session) {
        DatabaseProvider.runQuery {
            val dbSession = sessionsRepository.getSessionByUUID(session.uuid)
            dbSession?.let {
                downloadMeasurements(dbSession.id, session)
            }
        }
    }

    private fun downloadMeasurements(sessionId: Long, session: Session) {
        GlobalScope.launch(Dispatchers.Main) {
            val call = apiService.downloadSessionWithMeasurements(session.uuid)

            call.enqueue(DownloadMeasurementsCallback(
                sessionId, session, sessionsRepository, measurementStreamsRepository,
                measurementsRepository, errorHandler))
        }
    }
}


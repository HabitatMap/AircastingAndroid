package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.DatabaseProvider
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

class DownloadMeasurementsService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    private val sessionsRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val measurementsRepository = MeasurementsRepository()

    fun downloadMeasurements(session: Session, finallyCallback: (() -> Unit)? = null) {
        DatabaseProvider.runQuery {
            val dbSession = sessionsRepository.getSessionByUUID(session.uuid)
            dbSession?.let {
                downloadMeasurements(dbSession.id, session, finallyCallback)
            }
        }
    }

    private fun downloadMeasurements(sessionId: Long, session: Session, finallyCallback: (() -> Unit)?): Call<SessionWithMeasurementsResponse> {
        val lastMeasurementSyncTime = lastMeasurementTime(sessionId, session)

        val lastMeasurementSyncTimeString =
            DateConverter.toDateString(lastMeasurementSyncTime)
        val call =
            apiService.downloadMeasurements(session.uuid, lastMeasurementSyncTimeString)

        call.enqueue(DownloadMeasurementsCallback(
            sessionId, session, sessionsRepository, measurementStreamsRepository,
            measurementsRepository, errorHandler, finallyCallback))

        return call
    }

    private fun lastMeasurementTime(sessionId: Long, session: Session): Date {
        val lastMeasurementTime = measurementsRepository.lastMeasurementTime(sessionId)
        return LastMeasurementSyncCalculator.calculate(session.endTime, lastMeasurementTime)
    }
}


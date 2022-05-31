package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.data.api.response.SessionWithMeasurementsResponse
import retrofit2.Call

class DownloadMeasurementsService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    private val sessionsRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val activeMeasurementsRepository = ActiveSessionMeasurementsRepository()
    private val measurementsRepository = MeasurementsRepository()

    fun downloadMeasurements(localSession: LocalSession, finallyCallback: (() -> Unit)? = null) {
        DatabaseProvider.runQuery {
            val dbSession = sessionsRepository.getSessionWithMeasurementsByUUID(localSession.uuid)
            dbSession?.let {
                enqueueDownloadingMeasurements(dbSession, localSession, finallyCallback)
            }
        }
    }

    fun enqueueDownloadingMeasurements(dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject, localSession: LocalSession, finallyCallback: (() -> Unit)? = null): Call<SessionWithMeasurementsResponse>? {
        return when (localSession.type) {
            LocalSession.Type.MOBILE -> enqueueDownloadingMeasurementsForMobile(dbSessionWithMeasurements, localSession, finallyCallback)
            LocalSession.Type.FIXED -> enqueueDownloadingMeasurementsForFixed(dbSessionWithMeasurements, localSession, finallyCallback)
        }
    }

    fun enqueueDownloadingMeasurementsForMobile(dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject, localSession: LocalSession, finallyCallback: (() -> Unit)? = null): Call<SessionWithMeasurementsResponse>? {
        if (hasMeasurements(dbSessionWithMeasurements)) {
            finallyCallback?.invoke()
            return null
        }

        val call = apiService.downloadSessionWithMeasurements(localSession.uuid)

        val sessionId = dbSessionWithMeasurements.session.id

        call.enqueue(
            DownloadMeasurementsCallback(
            sessionId, localSession, sessionsRepository, measurementStreamsRepository, activeMeasurementsRepository,
            measurementsRepository, errorHandler, finallyCallback)
        )

        return call
    }

    private fun hasMeasurements(dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject): Boolean {
        return LocalSession(dbSessionWithMeasurements).hasMeasurements()
    }

    fun enqueueDownloadingMeasurementsForFixed(dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject, localSession: LocalSession, finallyCallback: (() -> Unit)? = null): Call<SessionWithMeasurementsResponse> {
        return enqueueDownloadingMeasurementsForFixed(dbSessionWithMeasurements.session.id, localSession, finallyCallback)
    }

    fun enqueueDownloadingMeasurementsForFixed(sessionId: Long, localSession: LocalSession, finallyCallback: (() -> Unit)? = null): Call<SessionWithMeasurementsResponse> {
        val lastMeasurementSyncTimeString = lastMeasurementTimeString(sessionId, localSession)

        val call =
            apiService.downloadFixedMeasurements(localSession.uuid, lastMeasurementSyncTimeString)

        call.enqueue(
            DownloadMeasurementsCallback(
            sessionId, localSession, sessionsRepository, measurementStreamsRepository, activeMeasurementsRepository,
            measurementsRepository, errorHandler, finallyCallback)
        )

        return call
    }

    private fun lastMeasurementTimeString(sessionId: Long, localSession: LocalSession): String {
        val lastMeasurementTime = measurementsRepository.lastMeasurementTime(sessionId)
        val lastMeasurementSyncTime =
            LastMeasurementSyncCalculator.calculate(localSession.endTime, lastMeasurementTime)
        return DateConverter.toDateString(lastMeasurementSyncTime)
    }
}


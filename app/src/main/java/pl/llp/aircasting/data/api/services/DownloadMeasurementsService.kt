package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.data_classes.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.local.repositories.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repositories.MeasurementsRepository
import pl.llp.aircasting.data.local.repositories.SessionsRepository
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.api.responses.SessionWithMeasurementsResponse
import retrofit2.Call

class DownloadMeasurementsService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    private val sessionsRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val activeMeasurementsRepository = ActiveSessionMeasurementsRepository()
    private val measurementsRepository = MeasurementsRepository()

    fun downloadMeasurements(session: Session, finallyCallback: (() -> Unit)? = null) {
        DatabaseProvider.runQuery {
            val dbSession = sessionsRepository.getSessionWithMeasurementsByUUID(session.uuid)
            dbSession?.let {
                enqueueDownloadingMeasurements(dbSession, session, finallyCallback)
            }
        }
    }

    fun enqueueDownloadingMeasurements(dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject, session: Session, finallyCallback: (() -> Unit)? = null): Call<SessionWithMeasurementsResponse>? {
        return when (session.type) {
            Session.Type.MOBILE -> enqueueDownloadingMeasurementsForMobile(dbSessionWithMeasurements, session, finallyCallback)
            Session.Type.FIXED -> enqueueDownloadingMeasurementsForFixed(dbSessionWithMeasurements, session, finallyCallback)
        }
    }

    fun enqueueDownloadingMeasurementsForMobile(dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject, session: Session, finallyCallback: (() -> Unit)? = null): Call<SessionWithMeasurementsResponse>? {
        if (hasMeasurements(dbSessionWithMeasurements)) {
            finallyCallback?.invoke()
            return null
        }

        val call = apiService.downloadSessionWithMeasurements(session.uuid)

        val sessionId = dbSessionWithMeasurements.session.id

        call.enqueue(
            DownloadMeasurementsCallback(
            sessionId, session, sessionsRepository, measurementStreamsRepository, activeMeasurementsRepository,
            measurementsRepository, errorHandler, finallyCallback)
        )

        return call
    }

    private fun hasMeasurements(dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject): Boolean {
        return Session(dbSessionWithMeasurements).hasMeasurements()
    }

    fun enqueueDownloadingMeasurementsForFixed(dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject, session: Session, finallyCallback: (() -> Unit)? = null): Call<SessionWithMeasurementsResponse> {
        return enqueueDownloadingMeasurementsForFixed(dbSessionWithMeasurements.session.id, session, finallyCallback)
    }

    fun enqueueDownloadingMeasurementsForFixed(sessionId: Long, session: Session, finallyCallback: (() -> Unit)? = null): Call<SessionWithMeasurementsResponse> {
        val lastMeasurementSyncTimeString = lastMeasurementTimeString(sessionId, session)

        val call =
            apiService.downloadFixedMeasurements(session.uuid, lastMeasurementSyncTimeString)

        call.enqueue(
            DownloadMeasurementsCallback(
            sessionId, session, sessionsRepository, measurementStreamsRepository, activeMeasurementsRepository,
            measurementsRepository, errorHandler, finallyCallback)
        )

        return call
    }

    private fun lastMeasurementTimeString(sessionId: Long, session: Session): String {
        val lastMeasurementTime = measurementsRepository.lastMeasurementTime(sessionId)
        val lastMeasurementSyncTime =
            LastMeasurementSyncCalculator.calculate(session.endTime, lastMeasurementTime)
        return DateConverter.toDateString(lastMeasurementSyncTime)
    }
}


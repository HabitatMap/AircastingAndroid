package pl.llp.aircasting.networking.services

import android.util.Log
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.data_classes.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.DateConverter
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.networking.responses.SessionWithMeasurementsResponse
import retrofit2.Call

class DownloadMeasurementsService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    private val sessionsRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
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
        Log.i("CALL", call.request().url().toString())
        val sessionId = dbSessionWithMeasurements.session.id

        call.enqueue(DownloadMeasurementsCallback(
            sessionId, session, sessionsRepository, measurementStreamsRepository,
            measurementsRepository, errorHandler, finallyCallback))

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

        call.enqueue(DownloadMeasurementsCallback(
            sessionId, session, sessionsRepository, measurementStreamsRepository,
            measurementsRepository, errorHandler, finallyCallback))

        return call
    }

    private fun lastMeasurementTimeString(sessionId: Long, session: Session): String {
        val lastMeasurementTime = measurementsRepository.lastMeasurementTime(sessionId)
        val lastMeasurementSyncTime = LastMeasurementSyncCalculator.calculate(session.endTime, lastMeasurementTime)
        return DateConverter.toDateString(lastMeasurementSyncTime)
    }
}


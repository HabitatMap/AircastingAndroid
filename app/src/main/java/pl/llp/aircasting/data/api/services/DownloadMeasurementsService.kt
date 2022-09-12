package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.response.SessionWithMeasurementsResponse
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.runOnIOThread
import retrofit2.Call

class DownloadMeasurementsService(
    private val apiService: ApiService,
    private val errorHandler: ErrorHandler
) {
    private val sessionsRepository = SessionsRepository()
    private val measurementStreamsRepository = MeasurementStreamsRepository()
    private val activeMeasurementsRepository = ActiveSessionMeasurementsRepository()
    private val measurementsRepository = MeasurementsRepository()

    fun downloadMeasurements(session: Session, finallyCallback: (() -> Unit)? = null) {
        runOnIOThread {
            val dbSession = sessionsRepository.getSessionWithMeasurementsByUUID(session.uuid)
            dbSession?.let {
                enqueueDownloadingMeasurements(dbSession, session, finallyCallback)
            }
        }
    }

    private fun enqueueDownloadingMeasurements(
        dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject,
        session: Session,
        finallyCallback: (() -> Unit)? = null
    ): Call<SessionWithMeasurementsResponse>? {
        return when (session.type) {
            Session.Type.MOBILE -> enqueueDownloadingMeasurementsForMobile(
                dbSessionWithMeasurements,
                session,
                finallyCallback
            )
            Session.Type.FIXED -> enqueueDownloadingMeasurementsForFixed(
                dbSessionWithMeasurements,
                session,
                finallyCallback
            )
        }
    }

    private fun enqueueDownloadingMeasurementsForMobile(
        dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject,
        session: Session,
        finallyCallback: (() -> Unit)? = null
    ): Call<SessionWithMeasurementsResponse>? {
        if (hasMeasurements(dbSessionWithMeasurements)) {
            finallyCallback?.invoke()
            return null
        }

        val call = apiService.downloadSessionWithMeasurements(session.uuid)

        val sessionId = dbSessionWithMeasurements.session.id

        call.enqueue(
            DownloadMeasurementsCallback(
                sessionId,
                session,
                sessionsRepository,
                measurementStreamsRepository,
                activeMeasurementsRepository,
                measurementsRepository,
                errorHandler,
                finallyCallback
            )
        )

        return call
    }

    private fun hasMeasurements(dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject): Boolean {
        return Session(dbSessionWithMeasurements).hasMeasurements()
    }

    private fun enqueueDownloadingMeasurementsForFixed(
        dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject,
        session: Session,
        finallyCallback: (() -> Unit)? = null
    ): Call<SessionWithMeasurementsResponse> {
        return enqueueDownloadingMeasurementsForFixed(
            dbSessionWithMeasurements.session.id,
            session,
            finallyCallback
        )
    }

    fun enqueueDownloadingMeasurementsForFixed(
        sessionId: Long,
        session: Session,
        finallyCallback: (() -> Unit)? = null
    ): Call<SessionWithMeasurementsResponse> {
        val lastMeasurementSyncTimeString = lastMeasurementTimeString(sessionId, session)

        val call =
            apiService.downloadFixedMeasurements(session.uuid, lastMeasurementSyncTimeString)

        call.enqueue(
            DownloadMeasurementsCallback(
                sessionId,
                session,
                sessionsRepository,
                measurementStreamsRepository,
                activeMeasurementsRepository,
                measurementsRepository,
                errorHandler,
                finallyCallback
            )
        )

        return call
    }

    private fun lastMeasurementTimeString(sessionId: Long, session: Session): String {
        val lastMeasurementTime = measurementsRepository.lastMeasurementTime(sessionId)
        val lastMeasurementSyncTime =
            LastMeasurementSyncCalculator.calculate(session.endTime, lastMeasurementTime)

        return LastMeasurementTimeStringFactory.get(lastMeasurementSyncTime, session.isExternal)
    }
}


package pl.llp.aircasting.data.api.services

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pl.llp.aircasting.data.api.response.SessionStreamWithMeasurementsResponse
import pl.llp.aircasting.data.api.response.SessionWithMeasurementsResponse
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.di.modules.IoDispatcher
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.exceptions.DBInsertException
import pl.llp.aircasting.util.exceptions.DownloadMeasurementsError
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelperDefault
import java.util.Date
import javax.inject.Inject

@UserSessionScope
class DownloadMeasurementsService @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepositoryImpl,
    private val activeMeasurementsRepository: ActiveSessionMeasurementsRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend fun downloadMeasurements(uuid: String) {
        sessionsRepository.getSessionWithMeasurementsByUUID(uuid)
            ?.let { dbSession ->
                downloadMeasurements(dbSession)
            }
    }

    private suspend fun downloadMeasurements(
        dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject,
    ) {
        when (dbSessionWithMeasurements.session.type) {
            Session.Type.MOBILE -> downloadMeasurementsForMobile(dbSessionWithMeasurements)
            Session.Type.FIXED -> downloadMeasurementsForFixed(dbSessionWithMeasurements.session)
        }
    }

    private suspend fun downloadMeasurementsForMobile(
        sessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject,
    ) = withContext(dispatcher) {
        sessionWithMeasurements.apply {
            runCatching {
                apiService.downloadSessionWithMeasurements(session.uuid)
            }.onSuccess {
                updateSessionData(
                    it,
                    session,
                    session.id,
                    saveMeasurements = sessionWithMeasurements.hasMeasurements
                )
            }.onFailure {
                errorHandler.handleAndDisplay(DownloadMeasurementsError(it))
            }
        }
    }

    private suspend fun downloadMeasurementsForFixed(
        session: SessionDBObject,
    ) = withContext(dispatcher) {
        val lastMeasurementSyncTimeString =
            lastMeasurementTimeString(session.id, session.endTime, session.isExternal)
        runCatching {
            apiService.downloadFixedMeasurements(
                session.uuid,
                lastMeasurementSyncTimeString
            )
        }.onSuccess {
            updateSessionData(it, session, session.id)
        }.onFailure {
            errorHandler.handleAndDisplay(DownloadMeasurementsError(it))
        }
    }

    private suspend fun lastMeasurementTimeString(
        sessionId: Long,
        endTime: Date?,
        isExternal: Boolean
    ): String {
        val lastMeasurementTime = measurementsRepository.lastMeasurementTime(sessionId)
        val lastMeasurementSyncTime =
            LastMeasurementSyncCalculator.calculate(endTime, lastMeasurementTime)

        return LastMeasurementTimeStringFactory.get(lastMeasurementSyncTime, isExternal)
    }

    private suspend fun updateSessionData(
        response: SessionWithMeasurementsResponse,
        session: SessionDBObject,
        sessionId: Long,
        saveMeasurements: Boolean = true,
    ) {
        deleteLocalStreamsNotPresentInResponse(sessionId, response.streams)
        if (saveMeasurements)
            saveSessionMeasurements(response, session, sessionId)
    }

    private suspend fun saveSessionMeasurements(
        response: SessionWithMeasurementsResponse,
        session: SessionDBObject,
        sessionId: Long
    ) {
        response.streams.let { streams ->
            val streamResponses = streams.values
            try {
                streamResponses.forEach { streamResponse ->
                    saveStreamData(streamResponse, session, sessionId)
                }
                updateSessionEndTime(session, response.end_time)
            } catch (e: SQLiteConstraintException) {
                errorHandler.handle(DBInsertException(e))
            }
        }
    }

    private suspend fun deleteLocalStreamsNotPresentInResponse(
        sessionId: Long,
        streams: HashMap<String, SessionStreamWithMeasurementsResponse>
    ) {
        val localStreams = measurementStreamsRepository.getSessionStreams(sessionId)
        val localSensors = localStreams.map { it.sensorName }
        val backendSensors = streams.values.map { it.sensorName }

        val sensorsToDelete = localSensors.filterNot { it in backendSensors }
        val streamsToDelete = localStreams.filter { it.sensorName in sensorsToDelete }

        measurementStreamsRepository.delete(streamsToDelete)
    }

    private suspend fun saveStreamData(
        streamResponse: SessionStreamWithMeasurementsResponse,
        session: SessionDBObject,
        sessionId: Long
    ) {
        val stream = MeasurementStream(streamResponse)
        val streamId = measurementStreamsRepository.getIdOrInsert(
            sessionId,
            stream
        )
        val averagingFrequency = MeasurementsAveragingHelperDefault().calculateAveragingWindow(
            session.startTime.time,
            measurementsRepository.lastMeasurementTime(sessionId)?.time
                ?: session.startTime.time
        ).value
        val measurements = MeasurementsFactory.get(
            streamResponse.measurements,
            averagingFrequency,
            session.isExternal
        )
        measurementsRepository.insertAll(streamId, sessionId, measurements)

        // We are using active_session_measurements table for following sessions to optimize the app's performance
        // Because of that when we launch the app after some time of inactivity we have to insert all
        // new measurements for following session to active_measurements_table apart from the basic measurements db table

        if (session.isFixed && session.isFollowed) {
            activeMeasurementsRepository.createOrReplaceMultipleRows(
                streamId,
                sessionId,
                measurements
            )
        }
    }

    private suspend fun updateSessionEndTime(
        dbSession: SessionDBObject,
        endTimeString: String?
    ) {
        endTimeString?.let {
            dbSession.copy(
                endTime = DateConverter.fromString(endTimeString)
            ).let {
                sessionsRepository.update(it)
            }
        }
    }
}
package pl.llp.aircasting.data.api.services

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pl.llp.aircasting.data.api.response.SessionStreamWithMeasurementsResponse
import pl.llp.aircasting.data.api.response.SessionWithMeasurementsResponse
import pl.llp.aircasting.data.local.entity.MeasurementStreamDBObject
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
        sessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject,
    ) {
        when (sessionWithMeasurements.session.type) {
            Session.Type.MOBILE -> downloadMeasurementsForMobile(sessionWithMeasurements)
            Session.Type.FIXED -> downloadMeasurementsForFixed(sessionWithMeasurements)
        }
    }

    private suspend fun downloadMeasurementsForMobile(
        sessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject,
    ) = withContext(dispatcher) {
        sessionWithMeasurements.apply {
            runCatching {
                apiService.downloadSessionWithMeasurements(session.uuid)
            }.onSuccess { response ->
                updateSessionData(
                    response,
                    sessionWithMeasurements,
                    shouldSaveMeasurements = hasNoMeasurements
                )
            }.onFailure {
                errorHandler.handleAndDisplay(DownloadMeasurementsError(it))
            }
        }
    }

    private suspend fun downloadMeasurementsForFixed(
        sessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject,
    ) = withContext(dispatcher) {
        sessionWithMeasurements.apply {
            val lastMeasurementSyncTimeString =
                lastMeasurementTimeString(session.id, session.endTime, session.isExternal)
            runCatching {
                apiService.downloadFixedMeasurements(
                    session.uuid,
                    lastMeasurementSyncTimeString
                )
            }.onSuccess {
                updateSessionData(it, sessionWithMeasurements)
            }.onFailure {
                errorHandler.handleAndDisplay(DownloadMeasurementsError(it))
            }
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
        sessionWithStreamsAndMeasurements: SessionWithStreamsAndMeasurementsDBObject,
        shouldSaveMeasurements: Boolean = true,
    ) {
        sessionWithStreamsAndMeasurements.apply {
            deleteLocalStreamsNotPresentInResponse(
                streams.map { it.stream },
                response.streams.values.map { it.sensorName })
            if (shouldSaveMeasurements)
                saveSessionMeasurements(
                    response,
                    session
                )
        }
    }

    private suspend fun saveSessionMeasurements(
        response: SessionWithMeasurementsResponse,
        session: SessionDBObject
    ) {
        response.streams.let { streams ->
            val streamResponses = streams.values
            try {
                streamResponses.forEach { streamResponse ->
                    saveStreamData(streamResponse, session)
                }
                updateSessionEndTime(session, response.end_time)
            } catch (e: SQLiteConstraintException) {
                errorHandler.handle(DBInsertException(e))
            }
        }
    }

    private suspend fun deleteLocalStreamsNotPresentInResponse(
        localStreams: List<MeasurementStreamDBObject>,
        backendSensors: List<String>,
    ) {
        val localSensors = localStreams.map { it.sensorName }

        val sensorsToDelete = localSensors.filterNot { it in backendSensors }
        val streamsToDelete = localStreams.filter { it.sensorName in sensorsToDelete }

        measurementStreamsRepository.delete(streamsToDelete)
    }

    private suspend fun saveStreamData(
        streamResponse: SessionStreamWithMeasurementsResponse,
        session: SessionDBObject
    ) {
        val stream = MeasurementStream(streamResponse)
        val streamId = measurementStreamsRepository.getIdOrInsert(
            session.id,
            stream
        )
        val averagingFrequency = MeasurementsAveragingHelperDefault().calculateAveragingWindow(
            session.startTime.time,
            measurementsRepository.lastMeasurementTime(session.id)?.time
                ?: session.startTime.time
        ).value
        val measurements = MeasurementsFactory.get(
            streamResponse.measurements,
            averagingFrequency,
            session.isExternal
        )
        measurementsRepository.insertAll(streamId, session.id, measurements)

        // We are using active_session_measurements table for following sessions to optimize the app's performance
        // Because of that when we launch the app after some time of inactivity we have to insert all
        // new measurements for following session to active_measurements_table apart from the basic measurements db table

        if (session.isFixed && session.isFollowed) {
            activeMeasurementsRepository.createOrReplaceMultipleRows(
                streamId,
                session.id,
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
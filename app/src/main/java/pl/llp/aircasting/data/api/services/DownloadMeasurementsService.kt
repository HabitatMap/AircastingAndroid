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
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.di.modules.IoDispatcher
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.exceptions.DBInsertException
import pl.llp.aircasting.util.exceptions.DownloadMeasurementsError
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelper
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

@UserSessionScope
class DownloadMeasurementsService @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeMeasurementsRepository: ActiveSessionMeasurementsRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val averagingHelper: MeasurementsAveragingHelper,
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
                    isAirBeamMiniV2 = false,
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
            val isAirBeamMini = streams.any { it.stream.sensorName.contains("AirBeamMini", true) }
            val isAirBeamMiniV2 = isAirBeamMini && (session.version >= 3 || session.sessionToken != null)
            val lastMeasurementSyncTimeString =
                lastMeasurementTimeString(session.id, session.endTime, session.is_indoor, isAirBeamMiniV2)
            runCatching {
                apiService.downloadFixedMeasurements(
                    session.uuid,
                    lastMeasurementSyncTimeString
                )
            }.onSuccess {
                updateSessionData(it, sessionWithMeasurements, isAirBeamMiniV2)
            }.onFailure {
                errorHandler.handleAndDisplay(DownloadMeasurementsError(it))
            }
        }
    }

    private suspend fun lastMeasurementTimeString(
        sessionId: Long,
        endTime: Date?,
        isIndoor: Boolean,
        isAirBeamMiniV2: Boolean,
    ): String {
        val lastMeasurementTime = measurementsRepository.lastMeasurementTime(sessionId)
        val lastMeasurementSyncTime =
            LastMeasurementSyncCalculator.calculate(endTime, lastMeasurementTime)

        return LastMeasurementTimeStringFactory.get(lastMeasurementSyncTime, beTimeZone(isIndoor, isAirBeamMiniV2))
    }

    private suspend fun updateSessionData(
        response: SessionWithMeasurementsResponse,
        sessionWithStreamsAndMeasurements: SessionWithStreamsAndMeasurementsDBObject,
        isAirBeamMiniV2: Boolean,
        shouldSaveMeasurements: Boolean = true,
    ) {
        sessionWithStreamsAndMeasurements.apply {
            deleteLocalStreamsNotPresentInResponse(
                streams.map { it.stream },
                response.streams.values.map { it.sensorName })
            if (shouldSaveMeasurements)
                saveSessionMeasurements(
                    response,
                    session,
                    isAirBeamMiniV2
                )
        }
    }

    private suspend fun saveSessionMeasurements(
        response: SessionWithMeasurementsResponse,
        session: SessionDBObject,
        isAirBeamMiniV2: Boolean,
    ) {
        response.streams.let { streams ->
            val streamResponses = streams.values
            try {
                streamResponses.forEach { streamResponse ->
                    saveStreamData(streamResponse, session, isAirBeamMiniV2)
                }
                updateSessionEndTime(session, response.end_time, isAirBeamMiniV2)
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
        session: SessionDBObject,
        isAirBeamMiniV2: Boolean,
    ) {
        val stream = MeasurementStreamDBObject(session.id, streamResponse)
        val streamId = measurementStreamsRepository.getIdOrInsert(
            session.id,
            stream
        )
        val averagingFrequency = averagingHelper.calculateAveragingWindow(
            session.startTime.time,
            measurementsRepository.lastMeasurementTime(session.id)?.time
                ?: session.startTime.time
        ).value
        val measurements = MeasurementsFactory.get(
            streamResponse.measurements,
            averagingFrequency,
            beTimeZone(session.is_indoor, isAirBeamMiniV2),
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
        endTimeString: String?,
        isAirBeamMiniV2: Boolean,
    ) {
        endTimeString?.let {
            // BE returns end_time as wall-clock numerals + literal "Z" suffix. The wall
            // clock is the session's TZ on BE: phone-default for mapped sessions, UTC for
            // indoor / locationless ones (see SessionDownloadService note). Parse with
            // the matching TZ so Date.time is the real instant.
            dbSession.copy(
                endTime = DateConverter.fromString(endTimeString, beTimeZone(dbSession.is_indoor, isAirBeamMiniV2))
            ).let {
                sessionsRepository.update(it)
            }
        }
    }

    // BE persists session/measurement timestamps as wall-clock numerals tagged with a
    // literal "Z". The wall clock is the session's `time_zone` on BE: mapped sessions
    // get a lat/lng-derived TZ (≈ phone-default), indoor / locationless sessions default
    // to UTC. Use this helper for every BE-facing parse/format on fixed sessions.
    private fun beTimeZone(isIndoor: Boolean, isAirBeamMiniV2: Boolean): TimeZone =
        if (isIndoor && isAirBeamMiniV2) TimeZone.getTimeZone("UTC") else TimeZone.getDefault()
}
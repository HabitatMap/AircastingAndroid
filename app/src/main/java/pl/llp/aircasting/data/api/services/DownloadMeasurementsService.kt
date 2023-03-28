package pl.llp.aircasting.data.api.services

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.response.SessionStreamResponse
import pl.llp.aircasting.data.api.response.SessionStreamWithMeasurementsResponse
import pl.llp.aircasting.data.api.response.SessionWithMeasurementsResponse
import pl.llp.aircasting.data.local.entity.MeasurementStreamDBObject
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.modules.IoDispatcher
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.events.LogoutEvent
import pl.llp.aircasting.util.exceptions.DBInsertException
import pl.llp.aircasting.util.exceptions.DownloadMeasurementsError
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelperDefault
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class DownloadMeasurementsService @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepositoryImpl,
    private val activeMeasurementsRepository: ActiveSessionMeasurementsRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    init {
        EventBus.getDefault().safeRegister(this)
    }

    private val callCanceled: AtomicBoolean = AtomicBoolean(false)

    suspend fun downloadMeasurements(session: Session) {
        val dbSession = sessionsRepository.getSessionWithMeasurementsByUUID(session.uuid)
        dbSession?.let {
            downloadMeasurements(session, dbSession)
        }
    }

    private suspend fun downloadMeasurements(
        session: Session,
        dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject,
    ) {
        when (session.type) {
            Session.Type.MOBILE -> downloadMeasurementsForMobile(
                session,
                dbSessionWithMeasurements
            )
            Session.Type.FIXED -> downloadMeasurementsForFixed(
                session,
                dbSessionWithMeasurements.session.id
            )
        }
    }

    private suspend fun downloadMeasurementsForMobile(
        session: Session,
        dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject,
    ) = withContext(dispatcher) {
        val sessionId = dbSessionWithMeasurements.session.id

        runCatching {
            apiService.downloadSessionWithMeasurements(session.uuid)
        }.onSuccess {
            val saveMeasurements = !hasMeasurements(dbSessionWithMeasurements)
            updateSessionData(it, session, sessionId, saveMeasurements)
        }.onFailure {
            errorHandler.handleAndDisplay(DownloadMeasurementsError(it))
        }
    }

    private fun hasMeasurements(dbSessionWithMeasurements: SessionWithStreamsAndMeasurementsDBObject): Boolean {
        return Session(dbSessionWithMeasurements).hasMeasurements()
    }

    suspend fun downloadMeasurementsForFixed(
        session: Session,
        sessionId: Long,
    ) = withContext(dispatcher) {
        val lastMeasurementSyncTimeString = lastMeasurementTimeString(sessionId, session)

        runCatching {
            apiService.downloadFixedMeasurements(
                session.uuid,
                lastMeasurementSyncTimeString
            )
        }.onSuccess {
            updateSessionData(it, session, sessionId)
        }.onFailure {
            errorHandler.handleAndDisplay(DownloadMeasurementsError(it))
        }
    }

    private suspend fun lastMeasurementTimeString(sessionId: Long, session: Session): String {
        val lastMeasurementTime = measurementsRepository.lastMeasurementTime(sessionId)
        val lastMeasurementSyncTime =
            LastMeasurementSyncCalculator.calculate(session.endTime, lastMeasurementTime)

        return LastMeasurementTimeStringFactory.get(lastMeasurementSyncTime, session.isExternal)
    }

    private suspend fun updateSessionData(
        response: SessionWithMeasurementsResponse,
        session: Session,
        sessionId: Long,
        saveMeasurements: Boolean = true,
    ) {
        deleteLocalStreamsNotPresentInResponse(sessionId, response.streams)
        if (saveMeasurements)
            saveSessionMeasurements(response, session, sessionId)
    }

    private suspend fun saveSessionMeasurements(
        response: SessionWithMeasurementsResponse,
        session: Session,
        sessionId: Long
    ) {
        response.streams.let { streams ->
            val streamResponses = streams.values
            if (!callCanceled.get()) {
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
    }

    private suspend fun deleteLocalStreamsNotPresentInResponse(
        sessionId: Long,
        streams: HashMap<String, SessionStreamWithMeasurementsResponse>
    ) {
        val localStreams = measurementStreamsRepository.getSessionStreams(sessionId)
        val backendStreams = streams.values.map {
            MeasurementStreamDBObject(
                sessionId,
                it as SessionStreamResponse
            )
        }
        val deletedStreams = localStreams.filterNot { it in backendStreams }
        measurementStreamsRepository.delete(deletedStreams)
    }

    private suspend fun saveStreamData(
        streamResponse: SessionStreamWithMeasurementsResponse,
        session: Session,
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

        if (session.isFixed() && session.followedAt != null) {
            activeMeasurementsRepository.createOrReplaceMultipleRows(
                streamId,
                sessionId,
                measurements
            )
        }
    }

    private suspend fun updateSessionEndTime(session: Session, endTimeString: String?) {
        if (endTimeString != null) session.endTime = DateConverter.fromString(endTimeString)
        sessionsRepository.update(session)
    }

    @Subscribe
    fun onMessageEvent(event: LogoutEvent) {
        callCanceled.set(true)
    }
}
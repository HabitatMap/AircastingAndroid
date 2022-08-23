package pl.llp.aircasting.data.api.services

import android.database.sqlite.SQLiteConstraintException
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.response.SessionStreamWithMeasurementsResponse
import pl.llp.aircasting.data.api.response.SessionWithMeasurementsResponse
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.events.LogoutEvent
import pl.llp.aircasting.util.exceptions.DBInsertException
import pl.llp.aircasting.util.exceptions.DownloadMeasurementsError
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.runOnIOThread
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.extensions.safeRegister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean

class DownloadMeasurementsCallback(
    private val sessionId: Long,
    private val session: Session,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val errorHandler: ErrorHandler,
    private val finallyCallback: (() -> Unit)? = null

) : Callback<SessionWithMeasurementsResponse> {
    val callCanceled = AtomicBoolean(false)
    val averagingService = AveragingService.get(sessionId)

    init {
        EventBus.getDefault().safeRegister(this)
    }

    override fun onResponse(
        call: Call<SessionWithMeasurementsResponse>,
        response: Response<SessionWithMeasurementsResponse>
    ) {
        if (response.isSuccessful) {
            val body = response.body()

            body?.streams?.let { streams ->
                runOnIOThread {
                    val streamResponses = streams.values
                    if (!callCanceled.get()) {
                        try {
                            streamResponses.forEach { streamResponse ->
                                saveStreamData(streamResponse)
                            }
                            updateSessionEndTime(body.end_time)
                        } catch (e: SQLiteConstraintException) {
                            errorHandler.handle(DBInsertException(e))
                        }
                    }

                    finallyCallback?.invoke()
                }
            }
        } else {
            errorHandler.handleAndDisplay(DownloadMeasurementsError())
            finallyCallback?.invoke()
        }
    }

    override fun onFailure(
        call: Call<SessionWithMeasurementsResponse>,
        t: Throwable
    ) {
        errorHandler.handleAndDisplay(DownloadMeasurementsError(t))
        finallyCallback?.invoke()
    }

    private fun saveStreamData(streamResponse: SessionStreamWithMeasurementsResponse) {
        val stream = MeasurementStream(streamResponse)
        val streamId = measurementStreamsRepository.getIdOrInsert(
            sessionId,
            stream
        )
        val averagingFrequency = averagingService?.currentAveragingThreshold()?.windowSize ?: 1
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
            activeSessionMeasurementsRepository.createOrReplaceMultipleRows(
                streamId,
                sessionId,
                measurements
            )
        }
    }

    private fun updateSessionEndTime(endTimeString: String?) {
        if (endTimeString != null) session.endTime = DateConverter.fromString(endTimeString)
        sessionsRepository.update(session)
    }

    @Subscribe
    fun onMessageEvent(event: LogoutEvent) {
        callCanceled.set(true)
    }
}

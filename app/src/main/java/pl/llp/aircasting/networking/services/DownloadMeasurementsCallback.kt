package pl.llp.aircasting.networking.services

import android.database.sqlite.SQLiteConstraintException
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.events.LogoutEvent
import pl.llp.aircasting.exceptions.DBInsertException
import pl.llp.aircasting.exceptions.DownloadMeasurementsError
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.DateConverter
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.models.Measurement
import pl.llp.aircasting.models.MeasurementStream
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.networking.responses.SessionStreamWithMeasurementsResponse
import pl.llp.aircasting.networking.responses.SessionWithMeasurementsResponse
import pl.llp.aircasting.services.AveragingService
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

): Callback<SessionWithMeasurementsResponse> {
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
                DatabaseProvider.runQuery {
                    val streamResponses = streams.values
                    if (!callCanceled.get()) {
                        try {
                            streamResponses.forEach { streamResponse ->
                                saveStreamData(streamResponse)
                            }
                            updateSessionEndTime(body?.end_time)
                        } catch( e: SQLiteConstraintException) {
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
        val measurements = streamResponse.measurements.map { response ->
            Measurement(response, averagingFrequency)
        }
        measurementsRepository.insertAll(streamId, sessionId, measurements)

        // We are using active_session_measurements table for following sessions to optimize the app's performance
        // Because of that when we launch the app after some time of inactivity we have to insert all
        // new measurements for following session to active_measurements_table apart from the basic measurements db table
        if (session.isFixed() && session.followedAt != null) {
            activeSessionMeasurementsRepository.createOrReplaceMultipleRows(streamId, sessionId, measurements)
        }
    }

    private fun updateSessionEndTime(endTimeString: String?) {
        if(endTimeString != null) session.endTime = DateConverter.get()?.fromString(endTimeString)
        sessionsRepository.update(session)
    }

    @Subscribe
    fun onMessageEvent(event: LogoutEvent) {
        callCanceled.set(true)
    }
}

package pl.llp.aircasting.data.api.services

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import org.apache.commons.lang3.time.DateUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.Constants
import pl.llp.aircasting.data.api.response.SessionStreamWithMeasurementsResponse
import pl.llp.aircasting.data.api.response.SessionWithMeasurementsResponse
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.api.response.SessionStreamWithMeasurementsResponse
import pl.llp.aircasting.data.api.response.SessionWithMeasurementsResponse
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.events.LogoutEvent
import pl.llp.aircasting.util.exceptions.DBInsertException
import pl.llp.aircasting.util.exceptions.DownloadMeasurementsError
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.safeRegister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
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
                DatabaseProvider.runQuery {
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
            val downloadedLastMeasurementTime =
                measurementsRepository.lastMeasurementTime(sessionId, streamId)
            val chartLastMeasurementTime =
                activeSessionMeasurementsRepository.lastMeasurementTime(sessionId, streamId)
            val timeDifference =
                chartLastMeasurementTime?.time?.let { downloadedLastMeasurementTime?.time?.minus(it) }

            if (oneHourHasElapsed(timeDifference)) {
                val lastMeasurementHour =
                    DateUtils.truncate(downloadedLastMeasurementTime, Calendar.HOUR_OF_DAY)
                val newMeasurements =
                    getNewMeasurementsForStreamStartingFromHour(streamId, timeDifference, lastMeasurementHour)

                activeSessionMeasurementsRepository.createOrReplaceMultipleRows(
                    streamId,
                    sessionId,
                    newMeasurements
                )
            }
        }
    }

    private fun oneHourHasElapsed(timeDifference: Long?): Boolean {
        return if (timeDifference == null) false
        else
            // Removing minute here to update on edge case when the minutes are the same, but
                // the difference is less than hour e.g. 7:59:59 -> 8:59:00
            timeDifference > (Constants.MILLIS_IN_HOUR - Constants.MILLIS_IN_MINUTE)
    }

    private fun getNewMeasurementsForStreamStartingFromHour(
        streamId: Long,
        timeDifference: Long?,
        lastMeasurementHour: Date
    ): List<Measurement> {
        if (timeDifference == null) return listOf()

        val hoursElapsed = timeDifference.toInt() / Constants.MILLIS_IN_HOUR

        val lastMeasurements = measurementsRepository.getLastMeasurementsForStreamStartingFromHour(
            streamId,
            Constants.MEASUREMENTS_IN_HOUR * (hoursElapsed),
            lastMeasurementHour
        ).asReversed()

        return lastMeasurements.map {
            if (it != null) {
                Measurement(it)
            } else {
                Log.d("Followed session", "There was Null measurement in the list")
                Measurement()
            }
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

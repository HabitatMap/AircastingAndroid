package pl.llp.aircasting.util.helpers.services

import android.util.Log
import kotlinx.coroutines.*
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Averaging for long mobile sessions
 *
 * General rules:
 * - for streams containing >2 hrs but <9 hrs of data apply a 5-second avg. time interval.
 * - for streams containing >9 hrs of data apply a 60-second avg. time interval
 *
 * Use case:
 * - the mobile active session reaches a duration of 2 hours and 5 seconds.
 * - at 2 hours and 5 seconds, the first 5-second average measurement is plotted on the map and graph.
 * - all of the data from the prior 2 hours is transformed into 5-second averages and the map and graph and stats are updated accordingly.
 *
 * Notes:
 * - averages should be attached to the middle value geocoordinates, and timestamp should be set to the interval end
 * i.e. if its a 5-second avg spanning the time frame 10:00:00 to 10:00:05,
 * the avg value gets pegged to the geocoordinates of middle measurement and timestamp from 10:00:05.
 * - thresholds are calculated based on ellapsed time of session, not based on actual measurements records
 * (pauses in sessions are not taken into account)
 *
 **/

class AveragingService(
    private val mMeasurementsRepository: MeasurementsRepositoryImpl,
    private val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    private val mSessionsRepository: SessionsRepository,
    private val helper: MeasurementsAveragingHelper,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val sessionUuidByAveragingJob: MutableMap<String, Job> = ConcurrentHashMap(),
) {
    companion object {
        fun getAveragingFrequency(
            firstMeasurement: Measurement?,
            lastMeasurement: Measurement?
        ): Int {
            firstMeasurement ?: return 0
            lastMeasurement ?: return 0

            val sessionDuration = lastMeasurement.time.time.minus(firstMeasurement.time.time)

            when {
                sessionDuration < TimeThreshold.FIRST.value -> return AveragingWindow.ZERO.value
                (sessionDuration > TimeThreshold.FIRST.value) && (sessionDuration < TimeThreshold.SECOND.value) -> return AveragingWindow.FIRST.value
                sessionDuration > TimeThreshold.SECOND.value -> return AveragingWindow.SECOND.value
            }
            return 0
        }
    }

    suspend fun stopAndPerformFinalAveraging(uuid: String?, window: AveragingWindow? = null) {
        Log.d(TAG, "Stopping averaging. Cancelling job: ${sessionUuidByAveragingJob[uuid]}")
        sessionUuidByAveragingJob[uuid]?.cancel()
        sessionUuidByAveragingJob.remove(uuid)

        val session = mSessionsRepository.getSessionByUUID(uuid) ?: return

        val currentWindow = if (window != null)
            window
        else {
            val lastMeasurementTime =
                mMeasurementsRepository.lastMeasurementTime(session.id) ?: return
            helper.calculateAveragingWindow(
                session.startTime.time,
                lastMeasurementTime.time
            )
        }
        Log.d(TAG, "Calculated current window: $currentWindow")
        perform(session, currentWindow)
        deleteLeftoverMeasurements(session, currentWindow)
    }

    private suspend fun deleteLeftoverMeasurements(
        session: SessionDBObject,
        currentWindow: AveragingWindow
    ) {
        val streamIds = mMeasurementStreamsRepository.getStreamsIdsBySessionId(session.id)
        streamIds.forEach { streamId ->
            val leftoverMeasurements =
                mMeasurementsRepository.getMeasurementsToAverage(streamId, currentWindow)
                    .map { it.id }
            mMeasurementsRepository.deleteMeasurementsSuspend(streamId, leftoverMeasurements)
        }
    }

    fun scheduleAveraging(sessionId: Long?) {
        sessionId ?: return

        coroutineScope.launch {
            val session = mSessionsRepository.getSessionByIdSuspend(sessionId)
            val sessionStart = session?.startTime ?: return@launch

            val fromSessionStartToFirstThreshold =
                (sessionStart.time + TimeThreshold.FIRST.value) - Date().time + 1000
            Log.d(
                TAG, "Scheduling periodic averaging for ${session.name} ${session.uuid}\n" +
                        "In ${fromSessionStartToFirstThreshold / 1000} seconds"
            )

            sessionUuidByAveragingJob[session.uuid] = launch {
                delay(fromSessionStartToFirstThreshold)
                startPeriodicAveraging(session.uuid, AveragingWindow.FIRST)
            }
        }
    }

    private fun CoroutineScope.startPeriodicAveraging(uuid: String, window: AveragingWindow) {
        sessionUuidByAveragingJob[uuid] = launch {
            Log.d(TAG, "Starting new periodic averaging in job: ${coroutineContext.job}")
            while (isActive) {
                val session = mSessionsRepository.getSessionByUUID(uuid) ?: return@launch
                Log.d(
                    TAG,
                    "Periodic averaging fired for ${session.name}\nIn job: ${coroutineContext.job}"
                )
                val lastMeasurementTime =
                    mMeasurementsRepository.lastMeasurementTime(session.id) ?: return@launch
                val currentWindow = helper.calculateAveragingWindow(
                    session.startTime.time,
                    lastMeasurementTime.time
                )
                Log.d(TAG, "Calculated current window: $currentWindow")

                if (currentWindow > window) {
                    coroutineScope.launch {
                        Log.d(TAG, "Cancelling job: ${sessionUuidByAveragingJob[uuid]}")
                        sessionUuidByAveragingJob[uuid]?.cancelAndJoin()
                        startPeriodicAveraging(uuid, currentWindow)
                    }
                }

                perform(session, currentWindow)
                delay(window.seconds)
            }
        }
    }

    private suspend fun perform(session: SessionDBObject, averagingWindow: AveragingWindow) {
        Log.d(TAG, "Performing with averaging window: $averagingWindow")
        val streamIds = mMeasurementStreamsRepository.getStreamsIdsBySessionId(session.id)
        streamIds.forEach { streamId ->
            val measurements =
                mMeasurementsRepository.getMeasurementsToAverage(streamId, averagingWindow)
            if (measurements.isEmpty()) return@forEach

            val intervalStart = session.startTime

            helper.averageMeasurements(
                measurements = measurements,
                startTime = intervalStart,
                averagingWindow = averagingWindow
            ) { averagedMeasurement, sourceMeasurements ->
                if (sourceMeasurements.isEmpty()) return@averageMeasurements

                val lastMeasurementIndex = sourceMeasurements.lastIndex
                sourceMeasurements[lastMeasurementIndex].value = averagedMeasurement.value
                sourceMeasurements[lastMeasurementIndex].time = averagedMeasurement.time
                sourceMeasurements[lastMeasurementIndex].averagingFrequency = averagingWindow.value

                if (sourceMeasurements.size <= 1) return@averageMeasurements
                val idsToDelete = sourceMeasurements
                    .slice(0 until lastMeasurementIndex)
                    .map { it.id }
                mMeasurementsRepository.deleteMeasurements(streamId, idsToDelete)
                sourceMeasurements[lastMeasurementIndex].apply {
                    Log.d(TAG, "Averaged measurement: ${Triple(id, time, averagingFrequency)}")
                    mMeasurementsRepository.averageMeasurement(id, value, averagingFrequency, time)
                }
            }
        }
    }
}

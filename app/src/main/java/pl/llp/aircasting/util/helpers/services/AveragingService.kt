package pl.llp.aircasting.util.helpers.services

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.util.extensions.addSeconds
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.util.extensions.truncateTo
import java.util.*
import java.util.Calendar.SECOND
import java.util.concurrent.atomic.AtomicBoolean

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
 * - averages should be attached to the middle value geocoordinates and timestamps,
 * i.e. if its a 5-second avg spanning the time frame 10:00:00 to 10:00:05,
 * the avg value gets pegged to the geocoordinates and timestamp from 10:00:03.
 * - thresholds are calculated based on ellapsed time of session, not based on actual measurements records
 * (pauses in sessions are not taken into account)
 * - if there are any final, unaveraged measurements on 2h+ or 9h+ session which would not fall into full averaging window
 * (5s or 60s) they should be deleted
 * - on threshold crossing (at 2h and at 9h into session) we also trim measurements that not fit into given window size
 *
 */

class AveragingService private constructor(
    private val sessionId: Long,
    private val mMeasurementsRepository: MeasurementsRepositoryImpl,
    private val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    private val mSessionsRepository: SessionsRepository,
) {
    private var mDBSession: SessionDBObject? = mSessionsRepository.getSessionById(sessionId)
    private var mNewAveragingThreshold = AtomicBoolean(false)
    private var mStreamIds: List<Long>? = getStreamIds()
    private var mPreviousAveragingFrequency: Int? = null

    private var finalAveragingThresholdIndex: Int? = null

    private val mFirstThresholdTime: Long =
        (mDBSession?.startTime ?: Date()).time + FIRST_THRESHOLD_TIME
    private val mSecondThresholdTime: Long =
        (mDBSession?.startTime ?: Date()).time + SECOND_THRESHOLD_TIME

    private var previousMeasurementsChunkTime: Date? = null
    private var currentMeasurementsChunkTime: Date? = null

    companion object {
        const val DEFAULT_FREQUENCY = 1
        const val FIRST_THRESHOLD_TIME = 2 * 60 * 60 * 1000 // 2 hours
        const val FIRST_THRESHOLD_FREQUENCY = 5
        const val SECOND_THRESHOLD_TIME = 9 * 60 * 60 * 1000 // 9 hours
        const val SECOND_THRESHOLD_FREQUENCY = 60

        private val THRESHOLDS = arrayOf(
            AveragingThreshold(
                windowSize = 1,
                time = 0
            ),
            AveragingThreshold(
                windowSize = FIRST_THRESHOLD_FREQUENCY,
                time = FIRST_THRESHOLD_TIME
            ),
            AveragingThreshold(
                windowSize = SECOND_THRESHOLD_FREQUENCY,
                time = SECOND_THRESHOLD_TIME
            )
        )

        /**
         * We keep separate singleton objects for each session in case someone is recording multiple mobile sessions
         */
        private var mSingletons: HashMap<Long, AveragingService?> = hashMapOf()

        fun get(
            sessionId: Long?,
            mMeasurementsRepository: MeasurementsRepositoryImpl = MeasurementsRepositoryImpl(),
            mMeasurementStreamsRepository: MeasurementStreamsRepository = MeasurementStreamsRepository(),
            mSessionsRepository: SessionsRepository = SessionsRepository(),
        ): AveragingService? {
            sessionId ?: return null

            if (mSingletons[sessionId] == null) {
                mSingletons[sessionId] = AveragingService(
                    sessionId,
                    mMeasurementsRepository,
                    mMeasurementStreamsRepository,
                    mSessionsRepository,
                )
            }

            return mSingletons[sessionId]
        }

        fun destroy(sessionId: Long?) {
            sessionId?.let { id ->
                mSingletons[id] = null
            }
        }

        fun getAveragingFrequency(
            firstMeasurement: Measurement?,
            lastMeasurement: Measurement?
        ): Int {
            firstMeasurement ?: return 0
            lastMeasurement ?: return 0

            val sessionDuration = lastMeasurement.time.time.minus(firstMeasurement.time.time)

            when {
                sessionDuration < FIRST_THRESHOLD_TIME -> return DEFAULT_FREQUENCY
                (sessionDuration > FIRST_THRESHOLD_TIME) && (sessionDuration < SECOND_THRESHOLD_TIME) -> return FIRST_THRESHOLD_FREQUENCY
                sessionDuration > SECOND_THRESHOLD_TIME -> return SECOND_THRESHOLD_FREQUENCY
            }
            return 0
        }

        fun getAveragingFrequency(firstMeasurementDate: Date?, lastMeasurementDate: Date?): Int {
            firstMeasurementDate ?: return DEFAULT_FREQUENCY
            lastMeasurementDate ?: return DEFAULT_FREQUENCY

            val sessionDuration = lastMeasurementDate.time.minus(firstMeasurementDate.time)

            return when {
                sessionDuration < FIRST_THRESHOLD_TIME -> return DEFAULT_FREQUENCY
                (sessionDuration > FIRST_THRESHOLD_TIME) && (sessionDuration < SECOND_THRESHOLD_TIME) -> return FIRST_THRESHOLD_FREQUENCY
                sessionDuration > SECOND_THRESHOLD_TIME -> return SECOND_THRESHOLD_FREQUENCY
                else -> DEFAULT_FREQUENCY
            }
        }
    }

    private fun streamIds(): List<Long>? {
        if (mStreamIds?.isEmpty() == true) {
            mStreamIds = getStreamIds()
        }

        return mStreamIds
    }

    private fun getStreamIds(): List<Long> {
        return mMeasurementStreamsRepository.getStreamsIdsBySessionIds(listOf(sessionId))
    }

    /**
     * Will perform measurements averaging on new (current) measurements. It will average all measurements recorded AFTER
     * crossing last averaging threshold that are not averaged yet and fall exactly into averaging windows of current frequency
     * e.g. if current averaging frequency is 5s and since last averaging we recorded 6 measurements, 5 measurements will be
     * averaged to 1 and 1 measurements will be left unaveraged. Measurements recorded BEFORE crossing last averaging threshold
     * will be averaged separately by [averagePreviousMeasurementsWithNewFrequency]
     *
     * @param isFinal False by default. If true, if will delete the remaining measurements not falling into exact window
     */
    fun perform(isFinal: Boolean = false) {
        val measurementsToAverage: HashMap<Long, List<MeasurementDBObject>?> = hashMapOf()

        // When while checking we find out the threshold has changed since last time checked
        // we will 1) update session averaging frequency in DB
        // 2) set mNewAveragingThreshold to true so we can perform averaging previous measurements
        checkDBAveragingFrequencyAndUpdateItIfNeeded()

        streamIds()?.forEach { streamId ->
            measurementsToAverage[streamId] = getCurrentMeasurementsToAverage(streamId)
        }

        if (currentAveragingThreshold().windowSize > 1) {
            averageMeasurements(
                measurementsToAverage,
                currentAveragingThreshold().windowSize,
                currentAveragingThreshold().windowSize,
                true,
                isFinal
            )
        }
    }

    private fun getCurrentMeasurementsToAverage(streamId: Long): List<MeasurementDBObject>? {
        return crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
            currentAveragingThreshold().let { currentAveragingThreshold ->
                mMeasurementsRepository.getNonAveragedCurrentMeasurements(
                    streamId,
                    currentAveragingThreshold.windowSize,
                    Date(crossingLastThresholdTime)
                )
            }
        }
    }

    private fun getPreviousMeasurementsToAverage(streamId: Long): List<MeasurementDBObject>? {
        return crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
            currentAveragingThreshold().let { currentAveragingThreshold ->
                mMeasurementsRepository.getNonAveragedPreviousMeasurements(
                    streamId,
                    currentAveragingThreshold.windowSize,
                    Date(crossingLastThresholdTime)
                )
            }
        }
    }

    private fun checkDBAveragingFrequencyAndUpdateItIfNeeded() {
        mDBSession = mSessionsRepository.getSessionById(sessionId)
        mDBSession?.averaging_frequency?.let { dbAveragingFrequency ->
            if (currentAveragingThreshold().windowSize > dbAveragingFrequency) {
                mPreviousAveragingFrequency = dbAveragingFrequency
                mNewAveragingThreshold.set(true)
                updateDBAveragingFrequency()
                refreshCurrentMeasurementsChunkTime()
            }
        }
    }

    private fun refreshCurrentMeasurementsChunkTime() {
        currentMeasurementsChunkTime = crossingLastThresholdDate()?.truncateTo(SECOND)
    }

    /**
     * Performs averaging in chunks of size given. It gets non averaged (or averaged with smaller frequency) measurements,
     * iterates through them in chinks and takes location from the middle one and average from all of the values
     * after averaging it deletes the rest of the measurements, leaving only the ones with averaged values
     *
     * @param averagingFrequency actual averaging frequency that we want to save to DB. It will be *current* frequency
     * @param windowSize actual window size we want to average. For second threshold, when reaveraging previously averaged
     *        measurements, we need to calculate it *current / previous*
     * @param isCurrent we pass true for current and false for averaging of previous measurements
     * @param isFinal false by default, will be true only when running final averaging on session finish (so we know whether
     *        we should trim measurements that cannot be averaed using full averaging window)
     */
    private fun averageMeasurements(
        measurementsToAverage: HashMap<Long, List<MeasurementDBObject>?>,
        averagingFrequency: Int,
        windowSize: Int,
        isCurrent: Boolean,
        isFinal: Boolean = false,
    ) {
        var measurementsInStreamToAverage: List<MeasurementDBObject>?

        val startingTime = getStartingTime(isCurrent, averagingFrequency)

        streamIds()?.forEach { streamId ->
            measurementsInStreamToAverage = measurementsToAverage[streamId]
            if (measurementsInStreamToAverage == null) {
                Log.d(TAG, "No measurements to average")
            } else {
                if (measurementsInStreamToAverage!!.size >= windowSize || isFinal) {
                    averageStreamMeasurements(
                        streamId,
                        measurementsInStreamToAverage!!,
                        windowSize,
                        averagingFrequency,
                        isCurrent,
                        isFinal,
                        startingTime
                    )
                }
            }
        }
    }

    private fun getStartingTime(
        isCurrent: Boolean,
        averagingFrequency: Int
    ) = if (isCurrent) {
        currentMeasurementsChunkTime =
            calendar().addSeconds(currentMeasurementsChunkTime, averagingFrequency)
        currentMeasurementsChunkTime
    } else {
        previousMeasurementsChunkTime =
            calendar().addSeconds(previousMeasurementsChunkTime, averagingFrequency)
        previousMeasurementsChunkTime
    }

    private fun averageStreamMeasurements(
        streamId: Long,
        measurements: List<MeasurementDBObject>,
        windowSize: Int,
        averagingFrequency: Int,
        isCurrent: Boolean,
        isFinal: Boolean = false,
        startingTime: Date?
    ) {
        var currentChunkTimeInStream = startingTime
        measurements.chunked(windowSize) { measurementsInWindow: List<MeasurementDBObject> ->
            if (measurementsInWindow.size == windowSize) {
                averageMeasurementsInWindow(
                    measurementsInWindow,
                    averagingFrequency,
                    streamId,
                    currentChunkTimeInStream
                )
                currentChunkTimeInStream =
                    calendar().addSeconds(currentChunkTimeInStream, averagingFrequency)
            } else {
                removeTrailingMeasurements(measurementsInWindow, streamId, isFinal, !isCurrent)
            }
        }
    }

    private fun removeTrailingMeasurements(
        trailingMeasurements: List<MeasurementDBObject>,
        streamId: Long,
        isFinalAveraging: Boolean,
        isPreviousMeasurementsAveraging: Boolean
    ) {
        // if this is final averaging after the session has finished OR it is averaging previous measurements
        // we want to delete remaining measurements instead of averaging smaller amount
        if (isFinalAveraging || isPreviousMeasurementsAveraging) {
            val trailingMeasurementsIds = trailingMeasurements.map { it.id }
            mMeasurementsRepository.deleteMeasurements(
                streamId,
                trailingMeasurementsIds
            )
        }
    }

    private fun averageMeasurementsInWindow(
        measurementsInWindow: List<MeasurementDBObject>,
        averagingFrequency: Int,
        streamId: Long,
        time: Date?
    ) {
        val measurementsToDeleteIds: List<Long>

        val averagedMeasurements = measurementsInWindow.toMutableList()
        val middleIndex = measurementsInWindow.size / 2
        val middle = averagedMeasurements.removeAt(middleIndex)
        val average =
            measurementsInWindow.sumOf { it.value } / measurementsInWindow.size
        val averagedMeasurementId = middle.id
//        Log.v(
//            TAG, "\"${mDBSession?.name}\" session averaged measurement:\n" +
//                    "Averaged Time: $time\n" +
//                    "Averaging frequency: $averagingFrequency\n" +
//                    "Measurement count in window: ${measurementsInWindow.size}\n" +
//                    "Stream ID: $streamId"
//        )
        measurementsToDeleteIds = averagedMeasurements.map { it.id }

        mMeasurementsRepository.deleteMeasurements(
            streamId,
            measurementsToDeleteIds
        )

//        Log.d(
//            "$TAG-DB",
//            "Measurements with sessionId pre average: $sessionId, streamId: $streamId, time: $time\n" +
//                    "$${mMeasurementsRepository.getMeasurements(sessionId, streamId, time)}"
//        )

        mMeasurementsRepository.averageMeasurement(
            averagedMeasurementId,
            average,
            averagingFrequency,
            time
        )
//
//        Log.d(
//            "$TAG-DB",
//            "Measurements with sessionId post average: $sessionId, streamId: $streamId, time: $time\n" +
//                    "$${mMeasurementsRepository.getMeasurements(sessionId, streamId, time)}"
//        )
    }

    fun performFinalAveragingAfterSDSync(averagingFrequencyIncludingSDCardMeasurements: Int) {
        finalAveragingThresholdIndex = THRESHOLDS.indexOf(
            THRESHOLDS.find { it.windowSize == averagingFrequencyIncludingSDCardMeasurements }
        )
        averagePreviousMeasurementsWithNewFrequency()
        perform(true)
    }

    fun averagePreviousMeasurementsWithNewFrequency() {
        checkDBAveragingFrequencyAndUpdateItIfNeeded()
        if (!mNewAveragingThreshold.get()) return

        var windowSize: Int? = null
        var previousWindowSize: Int? = null
        var averagingFrequency: Int? = 1
        var thresholdTime: Int? = null
        val measurementsToAverage: HashMap<Long, List<MeasurementDBObject>?> = hashMapOf()

        streamIds()?.forEach { streamId ->
            measurementsToAverage[streamId] = getPreviousMeasurementsToAverage(streamId)
        }

        val nonAveragedMeasurementsCount =
            crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
                mMeasurementsRepository.getNonAveragedPreviousMeasurementsCount(
                    sessionId,
                    Date(crossingLastThresholdTime),
                    currentAveragingThreshold().windowSize
                )
            }

        if ((nonAveragedMeasurementsCount ?: 0) > 0) {
            thresholdTime = currentAveragingThreshold().time
            previousWindowSize = mPreviousAveragingFrequency
                ?: THRESHOLDS[currentAveragingThresholdIndex() - 1].windowSize
            averagingFrequency = currentAveragingThreshold().windowSize
            windowSize = averagingFrequency / previousWindowSize
        }


        if (windowSize == null || thresholdTime == null || previousWindowSize == null || averagingFrequency == null) {
            Log.d(TAG, "No measurements to average")
        } else {
            crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
                refreshPreviousMeasurementsStartTime()
                averageMeasurements(
                    measurementsToAverage,
                    averagingFrequency,
                    windowSize,
                    false
                )
                mNewAveragingThreshold.set(false)
            }
        }
    }

    private fun refreshPreviousMeasurementsStartTime() {
        previousMeasurementsChunkTime = mDBSession?.startTime?.truncateTo(SECOND)
    }

    private fun updateDBAveragingFrequency() {
        currentAveragingThreshold().let { averagingThreshold ->
            mSessionsRepository.updateSessionAveragingFrequency(
                sessionId,
                averagingThreshold.windowSize
            )
        }
    }

    private fun crossingLastThresholdTime(): Long? {
        return when (currentAveragingThresholdIndex()) {
            2 -> mSecondThresholdTime
            1 -> mFirstThresholdTime
            else -> null
        }
    }

    private fun crossingLastThresholdDate(): Date? {
        return when (currentAveragingThresholdIndex()) {
            2 -> Date(mSecondThresholdTime)
            1 -> Date(mFirstThresholdTime)
            else -> null
        }
    }

    fun currentAveragingThreshold(): AveragingThreshold {
        return THRESHOLDS[currentAveragingThresholdIndex()]
    }

    private fun currentAveragingThresholdIndex(): Int {
        val finalIndex = finalAveragingThresholdIndex
        if (finalIndex != null && finalIndex != -1)
            return finalIndex

        val lastMeasurementTime = mMeasurementsRepository.lastMeasurementTime(sessionId) ?: Date()
        return when {
            lastMeasurementTime.time > mSecondThresholdTime -> 2
            lastMeasurementTime.time > mFirstThresholdTime -> 1
            else -> 0
        }
    }
}

class AveragingThreshold(val windowSize: Int, val time: Int)

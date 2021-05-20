package io.lunarlogic.aircasting.services

import android.util.Log
import io.lunarlogic.aircasting.database.data_classes.MeasurementDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import java.util.*
import kotlin.collections.HashMap

class AveragingService {
    private val sessionId: Long
    private val FIRST_TRESHOLD_TIME = 2 * 60 * 60 * 1000 // 2 hours
    private val SECOND_TRESHOLD_TIME = 9 * 60 * 60 * 1000 // 9 hours
    private val THRESHOLDS = arrayOf(
        AveragingThreshold(
            windowSize = 60,
            time = SECOND_TRESHOLD_TIME
        ),
        AveragingThreshold(
            windowSize = 5,
            time = FIRST_TRESHOLD_TIME
        ),
        AveragingThreshold(
            windowSize = 1,
            time = 0
        )
    )

    private val LOG_TAG = "AveragingService"

    private var mDBSession: SessionDBObject?

    private val measurementRepository = MeasurementsRepository()
    private val mMeasurementStreamsRepository = MeasurementStreamsRepository()
    private val mSessionsRepository = SessionsRepository()

    private val mFirstThresholdTime: Long
    private val mSecondThresholdTime: Long

    private var mNewAveragingThreshold: Boolean = false
    private var mStreamIds: List<Long>? = null

    private constructor(sessionId: Long) {
        this.sessionId = sessionId
        this.mDBSession = mSessionsRepository.getSessionById(sessionId)
        this.mStreamIds = getStreamIds()

        this.mFirstThresholdTime = (mDBSession?.startTime ?: Date()).time + FIRST_TRESHOLD_TIME
        this.mSecondThresholdTime = (mDBSession?.startTime ?: Date()).time + SECOND_TRESHOLD_TIME
    }

    companion object {
        private var mSingletons: HashMap<Long, AveragingService?> = hashMapOf()

        fun get(sessionId: Long): AveragingService {
            if (mSingletons[sessionId] == null) {
                mSingletons[sessionId] = AveragingService(sessionId)
            }

            return mSingletons[sessionId]!!
        }

        fun destroy(sessionId: Long?) {
            sessionId?.let { id ->
                mSingletons[id] = null
            }
        }
    }

    private fun getStreamIds() : List<Long>? {
        return mMeasurementStreamsRepository.getStreamsIdsBySessionIds(listOf(sessionId))
    }

    fun perform(final: Boolean = false) {
        var measurementsToAverage: HashMap<Long, List<MeasurementDBObject>?> = hashMapOf()

        setAveragingThreshold()

        mStreamIds?.forEach { streamId ->
            measurementsToAverage[streamId] = getCurrentMeasurementsToAverage(streamId)
        }

        if (currentAveragingThreshold().windowSize > 1) {
            val currentWindowSize = currentAveragingThreshold().windowSize
            val thresholdTime = currentAveragingThreshold().time

            if (currentWindowSize == null || thresholdTime == null) {
                Log.d(LOG_TAG, "No averaging will be performed - not enough measurements to average")
            } else {
                crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
                    averageMeasurements(
                        measurementsToAverage,
                        currentWindowSize,
                        currentWindowSize,
                        true,
                        final
                    )
                }

            }
        }
}

    private fun getCurrentMeasurementsToAverage(streamId: Long):  List<MeasurementDBObject>? {
        return crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
            currentAveragingThreshold()?.let { currentAveragingThreshold ->
                measurementRepository.getNonAveragedCurrentMeasurements(
                    streamId,
                    currentAveragingThreshold.windowSize,
                    Date(crossingLastThresholdTime)
                )
            }

        }
    }

    private fun getPreviousMeasurementsToAverage(streamId: Long):  List<MeasurementDBObject>? {
        return crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
            currentAveragingThreshold()?.let { currentAveragingThreshold ->
                measurementRepository.getNonAveragedPreviousMeasurements(
                    streamId,
                    currentAveragingThreshold.windowSize,
                    Date(crossingLastThresholdTime)
                )
            }

        }
    }

    private fun setAveragingThreshold() {
        mDBSession?.averaging_frequency?.let { dbAveragingFrequency ->
            if (currentAveragingThreshold()?.windowSize > dbAveragingFrequency) {
                mNewAveragingThreshold = true
                updateCurrentAveragingFrequency()
            }
        }
    }

    /**
     * Performs averaging in chunks of size given. It gets non averaged (or averaged with smaller frequency) measurements,
     * iterates through them in chinks and takes locatiom from the middle one and average from all of the values
     * after averaging it deletes the rest of the measuemrenst, leaving only the ones with averaged values
     *
     * @param previousWindowSize we use that to get measurements to average from DB
     *        in case of first averaging we pass 1 which is default frequency for new measurement records
     *        we also need that to reaverage previous measurements after reaching second threshold (we pass there previous
     *        frequency used for averaging)
     * @param windowSize actual window size we want to average. For second threshold, when reaveraging previously averaged
     *        measurements, we need to calculate it *current / previous*
     * @param averagingFrequency actual averaging frequency that we want to save to DB. It will be *current* frequency
     */
    private fun averageMeasurements(
        measurementsToAverage: HashMap<Long, List<MeasurementDBObject>?>,
        averagingFrequency: Int,
        windowSize: Int,
        current: Boolean,
        final: Boolean = false
    ) {
        var averagedMeasurementsIds: List<Long>
        var measurements: List<MeasurementDBObject>?

        if (mStreamIds?.isEmpty() ?: false) {
            mStreamIds = getStreamIds()
        }

        mStreamIds?.forEach { streamId ->
            println("MARYSIA: averaging measurements, current: ${current}")
            measurements = measurementsToAverage[streamId]

            if (measurements == null) {
                Log.d(LOG_TAG, "No measurements to average")
            } else {
                println("MARYSIA: averaging measurements count ${measurements!!.size}")
                if (measurements!!.size > windowSize || final) {
                    measurements?.let {
                        it.chunked(windowSize!!) { measurementsInWindow: List<MeasurementDBObject> ->
                            if (!current || (measurementsInWindow.size == windowSize)) {
                                averagedMeasurementsIds = mutableListOf<Long>()

                                val averagedMeasurements = measurementsInWindow.toMutableList()
                                val middleIndex = measurementsInWindow.size / 2
                                val middle = averagedMeasurements.removeAt(middleIndex)
                                val average =
                                    measurementsInWindow.sumByDouble { it.value } / measurementsInWindow.size
                                val averagedMeasurementId = middle.id
                                measurementRepository.averageMeasurement(
                                    averagedMeasurementId,
                                    average,
                                    averagingFrequency
                                )
                                averagedMeasurementsIds = averagedMeasurements.map { it.id }
                                measurementRepository.deleteMeasurements(
                                    streamId,
                                    averagedMeasurementsIds
                                )
                                println("MARYSIA: averaged measurement ${measurementsInWindow.map { "${it.value}}, " }} -> ${average}")
                            } else {
                                if (final) {
                                    println("MARYSIA: averaging final measurements deleting the remaining ${measurementsInWindow.size}")
                                    val finalMeasurementsIds = measurementsInWindow.map { it.id }
                                    measurementRepository.deleteMeasurements(
                                        streamId,
                                        finalMeasurementsIds
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    fun averagePreviousMeasurements() {
        println("MARYSIA: averaging historical measurements: ${mNewAveragingThreshold}")
        setAveragingThreshold()
        if (!mNewAveragingThreshold) return

        var windowSize: Int? = null
        var previousWindowSize: Int? = null
        var averagingFrequency: Int? = 1
        var thresholdTime: Int? = null
        var threshold: AveragingThreshold? = currentAveragingThreshold()
        var measurementsToAverage: HashMap<Long, List<MeasurementDBObject>?> = hashMapOf()

        if (mStreamIds?.isEmpty() ?: false) {
            mStreamIds = getStreamIds()
        }

        mStreamIds?.forEach { streamId ->
            measurementsToAverage[streamId] = getPreviousMeasurementsToAverage(streamId)
        }

        val nonAveragedMeasurementsCount =
            crossingLastThresholdTime()?.let {
                measurementRepository.getNonAveragedPreviousMeasurementsCount(
                    sessionId,
                    Date(it),
                    threshold?.windowSize ?: 1
                )
            }

        if (nonAveragedMeasurementsCount ?: 0 > 0) {
            thresholdTime = threshold?.time
            previousWindowSize = THRESHOLDS[currentAveragingThresholdIndex() + 1].windowSize
            averagingFrequency = threshold?.windowSize
            windowSize = averagingFrequency!! / previousWindowSize
        }


        if (windowSize == null || thresholdTime == null || previousWindowSize == null || averagingFrequency == null) {
            Log.d(LOG_TAG, "No measurements to average")
        } else {
            crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
                averageMeasurements(
                    measurementsToAverage,
                    averagingFrequency,
                    windowSize,
                    false
                )
                mNewAveragingThreshold = false
            }
        }
    }

    private fun updateCurrentAveragingFrequency() {
        mDBSession?.let {session ->
            currentAveragingThreshold()?.let { averagingThreshold ->
                mSessionsRepository.updateSessionAveragingFrequency(session.id, averagingThreshold.windowSize)
            }
        }
    }

    private fun crossingLastThresholdTime() : Long? {
        return when (currentAveragingThresholdIndex()) {
            0 -> mSecondThresholdTime
            1 -> mFirstThresholdTime
            else -> null
        }
    }

    fun currentAveragingThreshold() : AveragingThreshold {
        return THRESHOLDS[currentAveragingThresholdIndex()]
    }

    private fun currentAveragingThresholdIndex() : Int {
        return when {
            Date().time > mSecondThresholdTime -> 0
            Date().time > mFirstThresholdTime -> 1
            else -> 2
        }
    }
}

class AveragingThreshold(val windowSize: Int, val time: Int)

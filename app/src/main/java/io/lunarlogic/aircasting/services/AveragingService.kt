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
    private var measurementsToAverage: List<MeasurementDBObject>? = null
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
        println("MARYSIA: mFirstThresholdTime = ${mFirstThresholdTime}")
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
    fun perform(): Int {
        var dbSessionId: Long? = null
        var averagingFrequency: Int = 1
        var averagedMeasurementsIds: MutableList<Long> = mutableListOf()
        var thresholdTime: Int? = null

        dbSessionId = sessionId


        dbSessionId?.let { sessionId ->
            setAveragingThreshold()
        }

        //  here average current measurements

        mStreamIds?.forEach { streamId ->
            if (currentAveragingThreshold() != null && currentAveragingThreshold().windowSize!! > 1) {

                // TODO here we should use different method to get measurements (after crossing threshold)
                measurementsToAverage =
                    crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
                        measurementRepository.getNonAveragedCurrentMeasurements(
                            streamId,
                            currentAveragingThreshold()?.windowSize!!,
                            Date(crossingLastThresholdTime)
                        )
                    }
                val size = measurementsToAverage?.size ?: 0
                val currentWindowSize = currentAveragingThreshold()?.windowSize ?: 1
                if (size > currentWindowSize) {
                    println("MARYSIA: averaging threshold window: ${currentAveragingThreshold().windowSize}")
                    thresholdTime = currentAveragingThreshold()?.time
                    averagingFrequency = currentWindowSize
                    println("MARYSIA: averagingFrequency: ${averagingFrequency}")
                    val windowSize = currentWindowSize
                    println("MARYSIA: window size: ${windowSize}")

                }

                if (currentWindowSize == null || thresholdTime == null) {
                    Log.d(LOG_TAG, "No measurements to average")
                } else {
                    crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
                        averageMeasurements(1, currentWindowSize, currentWindowSize, crossingLastThresholdTime, true)
                    }

                }
            }
        }

    return 0
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
     * Perofms averaging in chunks of size given. It gets non averaged (or averaged with smaller frequency) measurements,
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
        previousWindowSize: Int,
        windowSize: Int,
        averagingFrequency: Int,
        // maybe we should pass measurements here already
        thresholdCrossingTime: Long,
        current: Boolean
    ) {
        var averagedMeasurementsIds = listOf<Long>()
        if (mStreamIds?.isEmpty() ?: false) {
            mStreamIds = getStreamIds()
        }

        mStreamIds?.forEach { streamId ->
            println("MARYSIA: averaging measurements, current: ${current}")
            measurementsToAverage = if (current) {
                measurementRepository.getNonAveragedCurrentMeasurements(
                    streamId,
                    averagingFrequency,
                    Date(thresholdCrossingTime)
                )
            } else {
                measurementRepository.getNonAveragedPreviousMeasurements(
                    streamId,
                    averagingFrequency,
                    Date(thresholdCrossingTime)
                )
            }

            if (measurementsToAverage == null) {
                Log.d(LOG_TAG, "No measurements to average")
            } else {
                println("MARYSIA: averaging measurements count ${measurementsToAverage!!.size}")
                if (measurementsToAverage!!.size > windowSize!!) {
                    measurementsToAverage?.let {
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
                            }

                        }
                    }

                    // We remove all of the measurements in windows apart from the middle ones

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

        var averagedCount = 0
        var averagedMeasurementsIds: MutableList<Long> = mutableListOf()


        val nonAveragedMeasurementsCount =
            crossingLastThresholdTime()?.let {
                measurementRepository.getNonAveragedPreviousMeasurementsCount(
                    sessionId,
                    Date(it),
                    threshold?.windowSize ?: 1
                )
            }

        if (nonAveragedMeasurementsCount ?: 0 > 0) {
            println("MARYSIA: averaging threshold window: ${threshold?.windowSize}")
            thresholdTime = threshold?.time
            previousWindowSize = THRESHOLDS[currentAveragingThresholdIndex() + 1].windowSize
            averagingFrequency = threshold?.windowSize
            println("MARYSIA: averagingFrequency: ${averagingFrequency}")
            windowSize = averagingFrequency!! / previousWindowSize!!
            println("MARYSIA: window size: ${windowSize}")

        }

        if (windowSize == null || thresholdTime == null || previousWindowSize == null || averagingFrequency == null) {
            Log.d(LOG_TAG, "No measurements to average")
        } else {
            crossingLastThresholdTime()?.let { crossingLastThresholdTime ->
                averageMeasurements(previousWindowSize, windowSize, averagingFrequency,crossingLastThresholdTime, false)
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
        return if (currentAveragingThresholdIndex() == 0) {
            secondThresholdTime()
        } else if (currentAveragingThresholdIndex() == 1) {
            firstThresholdTime()
        } else {
            null
        }
    }

    fun currentAveragingThreshold() : AveragingThreshold {
        return if (Date().time > secondThresholdTime()) {
            THRESHOLDS[0]
        } else if(Date().time > firstThresholdTime()) {
            THRESHOLDS[1]
        } else {
            THRESHOLDS[2]
        }
    }

    private fun currentAveragingThresholdIndex() : Int {
        return if (Date().time > secondThresholdTime()) {
            0
        } else if(Date().time > firstThresholdTime()) {
            1
        } else {
            2
        }
    }

    private fun firstThresholdTime() : Long {
        return mFirstThresholdTime
    }

    private fun secondThresholdTime() : Long {
        return mSecondThresholdTime
    }
}

class AveragingThreshold(val windowSize: Int, val time: Int)

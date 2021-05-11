package io.lunarlogic.aircasting.lib

import android.util.Log
import io.lunarlogic.aircasting.database.data_classes.MeasurementDBObject
import io.lunarlogic.aircasting.database.data_classes.SessionDBObject
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import java.util.*

class AveragingService(
    val sessionId: Long
) {
    private val FIRST_TRESHOLD_TIME = 2 * 60 * 1000 //2 * 60 * 60 * 1000 // 2 hours
    private val SECOND_TRESHOLD_TIME = 4 * 60 * 1000 //9 * 60 * 60 * 1000 // 9 hours
    private val THRESHOLDS = arrayOf(
        AveragingThreshold(windowSize = 60, time = SECOND_TRESHOLD_TIME),
        AveragingThreshold(windowSize = 5, time = FIRST_TRESHOLD_TIME),
        AveragingThreshold(windowSize = 1, time = 0)
    )

    private val LOG_TAG = "AveragingService"

    private var mDBSession: SessionDBObject?
    private var measurementsToAverage: List<MeasurementDBObject>? = null
    private val measurementRepository = MeasurementsRepository()
    private val mMeasurementStreamsRepository = MeasurementStreamsRepository()
    private val mSessionsRepository = SessionsRepository()
    private var mCurrentAveragingThreshold: AveragingThreshold? = null
    private var mCurrentAveragingThresholdIndex: Int = 2
    private var newAveragingThreshold: Boolean = false
    var mStreamIds: List<Long>? = null

    init {
        mDBSession = mSessionsRepository.getSessionById(sessionId)
        mStreamIds =
            mMeasurementStreamsRepository.getStreamsIdsBySessionIds(listOf(sessionId))
    }

    fun perform(): Int {
        var dbSessionId: Long? = null
        var averagingFrequency: Int = 1
        var averagedMeasurementsIds: MutableList<Long> = mutableListOf()
        var thresholdTime: Int? = null

        dbSessionId = sessionId


        dbSessionId?.let { sessionId ->
            setAveragingThreshold()
            if (newAveragingThreshold) {
                // if we changed threshold frequency for bigger than 1 we should recalculate all previpus measurements
                averagePreviousMeasurements()
                newAveragingThreshold = false
            }
        }

        // TODO here average current measurements
        mStreamIds?.forEach { streamId ->
            if (mCurrentAveragingThreshold != null && mCurrentAveragingThreshold?.windowSize!! > 1) {

                measurementsToAverage =
                    measurementRepository.getNonAveragedMeasurements(
                        streamId,
                        1
                    )
                val size = measurementsToAverage?.size ?: 0
                val currentWindowSize = mCurrentAveragingThreshold?.windowSize ?: 1
                if (size > currentWindowSize) {
                    println("MARYSIA: averaging threshold window: ${mCurrentAveragingThreshold?.windowSize}")
                    thresholdTime = mCurrentAveragingThreshold?.time
                    averagingFrequency = currentWindowSize
                    println("MARYSIA: averagingFrequency: ${averagingFrequency}")
                    val windowSize = currentWindowSize
                    println("MARYSIA: window size: ${windowSize}")

                }

                if (currentWindowSize == null || thresholdTime == null) {
                    Log.d(LOG_TAG, "No measurements to average")
                } else {
                    averageMeasurements(1, currentWindowSize, currentWindowSize)

                }
            }
        }




    return 0
}

    private fun setAveragingThreshold() {
        var sessionLength: Long = 0

        THRESHOLDS.forEachIndexed { index, threshold ->
            val sessionEndTime = mDBSession?.endTime ?: Date()
            if (sessionEndTime != null && mDBSession?.startTime != null) {
                sessionLength = sessionEndTime.time - mDBSession?.startTime!!.time
            }
            println("MARYSIA: startTime ${mDBSession?.startTime} endTime ${sessionEndTime} length ${sessionLength}")

            if (threshold.windowSize == mDBSession?.averaging_frequency) {
                mCurrentAveragingThreshold = threshold
            }
            // checking if we reached next averaging threshold
            if (sessionLength > threshold.time) {
                mDBSession?.averaging_frequency?.let { currentSessionAveragingFrequency ->
                    if (threshold.windowSize > currentSessionAveragingFrequency) {
                        mCurrentAveragingThreshold = threshold
                        mCurrentAveragingThresholdIndex = index
                        newAveragingThreshold = true
                        updateCurrentAveragingFrequency()
                    }
                }
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
        averagingFrequency: Int
    ) {
        var averagedMeasurementsIds = listOf<Long>()

        mStreamIds?.forEach { streamId ->
            measurementsToAverage =
                measurementRepository.getNonAveragedMeasurements(
                    streamId,
                    previousWindowSize
                )

            if (measurementsToAverage == null) {
                Log.d(LOG_TAG, "No measurements to average")
            } else {
                if (measurementsToAverage!!.size > windowSize!!) {
                    measurementsToAverage?.let {
                        it.chunked(windowSize!!) { measurementsInWindow: List<MeasurementDBObject> ->
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

                    // We remove all of the measurements in windows apart from the middle ones

                }
            }



        }
    }
    private fun averagePreviousMeasurements() {
        if (mCurrentAveragingThresholdIndex == 2) return // in case we run this before reaching first or second threshold
        println("MARYSIA: averaging historical measurements:")

        var windowSize: Int? = null
        var previousWindowSize: Int? = null
        var averagingFrequency: Int = 1
        var thresholdTime: Int? = null

        var averagedCount = 0
        var averagedMeasurementsIds: MutableList<Long> = mutableListOf()

        val nonAveragedMeasurementsCount =
            measurementRepository.getNonAveragedMeasurementsCount(
                sessionId,
                THRESHOLDS[mCurrentAveragingThresholdIndex + 1].windowSize
            )

        val threshold = THRESHOLDS[mCurrentAveragingThresholdIndex]
        if (nonAveragedMeasurementsCount > 0) {
            println("MARYSIA: averaging threshold window: ${threshold.windowSize}")
            thresholdTime = threshold.time
            previousWindowSize = THRESHOLDS[mCurrentAveragingThresholdIndex + 1].windowSize
            averagingFrequency = threshold.windowSize
            println("MARYSIA: averagingFrequency: ${averagingFrequency}")
            windowSize = averagingFrequency / previousWindowSize!!
            println("MARYSIA: window size: ${windowSize}")

        }

        if (windowSize == null || thresholdTime == null || previousWindowSize == null) {
            Log.d(LOG_TAG, "No measurements to average")
        } else {
            averageMeasurements(previousWindowSize, windowSize, averagingFrequency)
        }
    }

    private fun updateCurrentAveragingFrequency() {
        mDBSession?.let {session ->
            mCurrentAveragingThreshold?.let { averagingThreshold ->
                mSessionsRepository.updateSessionAveragingFrequency(session.id, averagingThreshold.windowSize)
            }
        }

    }
}

class AveragingThreshold(val windowSize: Int, val time: Int)

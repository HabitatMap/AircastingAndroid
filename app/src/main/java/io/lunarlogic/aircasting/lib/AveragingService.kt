package io.lunarlogic.aircasting.lib

import android.util.Log
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.data_classes.MeasurementDBObject
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.Session
import kotlinx.coroutines.cancel
import java.util.*
import kotlin.collections.HashMap

class AveragingService(
    val sessionId: Long
) {
    private val FIRST_TRESHOLD_TIME =  10000//2 * 60 * 60 * 1000 // 2 hours
    private val SECOND_TRESHOLD_TIME = 20000//9 * 60 * 60 * 1000 // 9 hours
    private val THRESHOLDS = arrayOf(AveragingThreshold(windowSize = 60, time = SECOND_TRESHOLD_TIME),
        AveragingThreshold(windowSize = 5, time = FIRST_TRESHOLD_TIME),
        AveragingThreshold(windowSize = 1, time = 0))

    private val LOG_TAG = "AveragingService"

    private var measurementsToAverage: List<MeasurementDBObject>? = null
    private val measurementRepository = MeasurementsRepository()
    private val mMeasurementStreamsRepository = MeasurementStreamsRepository()
    private val mSessionsRepository = SessionsRepository()

    fun averageMeasurements(): Int {
        var dbSessionId: Long? = null
        var startAveragingTime: Date? = null
        var thresholdTime: Int? = null
        var windowSize: Int? = null
        var previousWindowSize: Int? = null
        var averagingFrequency: Int = 1
        var averagedCount = 0
        var nonAveragedMeasurementsCount = 0
        var streamIds: List<Long>? = null
        var sessionLength: Long = 0

            dbSessionId = sessionId
            val session = mSessionsRepository.getSessionById(sessionId)

            dbSessionId?.let { sessionId ->
                THRESHOLDS.forEachIndexed { index, threshold ->
                    val sessionEndTime = Date() // TODO make sure we take always endTime of the sessin
//                    val lastMeasurementTime = measurementRepository.lastMeasurementTime(dbSessionId)
                    if (session?.endTime != null && session?.startTime != null) {
                        sessionLength = session.endTime.time - session.startTime.time
                    }
                    println("MARYSIA: startTime ${session?.startTime} endTime ${session?.endTime} length ${sessionLength}")
                    if (sessionLength > threshold.time) {
                        println("MARYSIA: reached threshold with window ${threshold.windowSize}")
                    }
                    if (index < 2) {
                        nonAveragedMeasurementsCount =
                            measurementRepository.getNonAveragedMeasurementsCount(
                                sessionId,
                                THRESHOLDS[index + 1].windowSize
                            )

                        println("MARYSIA: measurements to average: ${nonAveragedMeasurementsCount}")
                    }

                    if (nonAveragedMeasurementsCount > 0) {
                        println("MARYSIA: averaging threshold window: ${threshold.windowSize}")
                        thresholdTime = threshold.time
                        previousWindowSize = THRESHOLDS[index + 1].windowSize
                        averagingFrequency = threshold.windowSize
                        println("MARYSIA: averagingFrequency: ${averagingFrequency}")
                        windowSize = averagingFrequency / previousWindowSize!!
                        println("MARYSIA: window size: ${windowSize}")

                    }
                }

                if (windowSize == null || thresholdTime == null) {
                    Log.d(LOG_TAG, "No measurements to average")
                } else {

                    streamIds =
                        mMeasurementStreamsRepository.getStreamsIdsBySessionIds(listOf(sessionId))
                val sessionEndTime = Date() //TODO make sure we take always endTime of the sessin
                startAveragingTime = Date(sessionEndTime.time - thresholdTime!!)


                streamIds?.forEach { streamId ->
                    println("MARYSIA: averaging historical measurements:")
                    measurementsToAverage =
                        measurementRepository.getNonAveragedMeasurements(
                            streamId,
                            previousWindowSize ?: 1
                        )

                    if (measurementsToAverage == null) {
                        Log.d(LOG_TAG, "No measurements to average")
                    } else {
                        if (measurementsToAverage!!.size > windowSize!!) {
                            measurementsToAverage?.let {
                                it.chunked(windowSize!!) { measurementsInWindow: List<MeasurementDBObject> ->
                                    averagedCount += windowSize!!
                                    val middleIndex = measurementsInWindow.size / 2
                                    val middle = measurementsInWindow[middleIndex]
                                    val average =
                                        measurementsInWindow.sumByDouble { it.value } / measurementsInWindow.size
                                    val averagedMeasurementId = middle.id
                                    measurementRepository.averageMeasurement(
                                        averagedMeasurementId,
                                        average,
                                        averagingFrequency
                                    )
                                    println("MARYSIA: averaged measurement ${measurementsInWindow.map { "${it.value}}, " }} -> ${average}")
                                }
                            }

                            measurementRepository.deleteMeasurementsAfterAveraging(
                                streamId,
                                startAveragingTime!!,
                                previousWindowSize ?: 1
                            )
                        }
                    }

                    if (averagingFrequency > 1) {

                        measurementsToAverage =
                            measurementRepository.getNonAveragedMeasurements(
                                streamId,
                                1
                            )
                        println("MARYSIA: we should also average new measurements:${measurementsToAverage?.size}")
                    }

                    }
                }
            }
        return averagedCount
    }
}

class AveragingThreshold(val windowSize: Int, val time: Int)

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
    private val FIRST_TRESHOLD_TIME = 2 * 60 * 60 * 1000
    private val SECOND_TRESHOLD_TIME = 8 * 60 * 60 * 1000
    private val THRESHOLDS = listOf<AveragingThreshold>(
        AveragingThreshold(windowSize = 60, time = SECOND_TRESHOLD_TIME),
        AveragingThreshold(windowSize = 5, time = FIRST_TRESHOLD_TIME)
    )

    private val LOG_TAG = "AveragingService"

    private var measurementsToAverage: List<MeasurementDBObject>? = null
    private val measurementRepository = MeasurementsRepository()
    private val mMeasurementStreamsRepository = MeasurementStreamsRepository()

    fun averageMeasurements(): Int {
        var dbSessionId: Long? = null
        var startAveragingTime: Date? = null
        var thresholdTime: Int? = null
        var windowSize: Int? = null
        var averagedCount = 0
        var nonAveragedMeasurementsCount = 0
        var streamIds: List<Long>? = null

            dbSessionId = sessionId

            dbSessionId?.let { sessionId ->
                THRESHOLDS.forEach { threshold ->
                    val sessionEndTime = Date() // TODO make sure we take always endTime of the sessin
                        nonAveragedMeasurementsCount =
                            measurementRepository.getNonAveragedMeasurementsCount(
                                sessionId,
                                Date(sessionEndTime.time - threshold.time)
                            )
                    if (nonAveragedMeasurementsCount > 0) {
                        println("MARYSIA: averaging threshold window: ${threshold.windowSize}")
                        thresholdTime = threshold.time
                        windowSize = threshold.windowSize
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
                    measurementsToAverage =
                        measurementRepository.getNonAveragedMeasurementsOlderThan(
                            streamId,
                            startAveragingTime!!
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
                                        average
                                    )
                                    println("MARYSIA: averaged measurement ${measurementsInWindow.map { "${it.value}}, " }} -> ${average}")
                                }
                            }

                            measurementRepository.deleteMeasurementsAfterAveraging(
                                streamId,
                                startAveragingTime!!
                            )
                        }
                    }

                    }
                }
            }
        return averagedCount
    }
}

class AveragingThreshold(val windowSize: Int, val time: Int)

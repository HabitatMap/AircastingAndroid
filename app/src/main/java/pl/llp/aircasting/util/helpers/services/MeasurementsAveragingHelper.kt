package pl.llp.aircasting.util.helpers.services

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.util.extensions.truncateTo
import java.util.*
import java.util.Calendar.SECOND
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.round

enum class AveragingWindow(val value: Int) {
    ZERO(120),
    FIRST(120),
    SECOND(120);

    val seconds: Long = value * 1000L
}

enum class TimeThreshold(val value: Long) {
    // Two hours
    FIRST(2 * 60 * 60 * 1000),

    // Nine hours
    SECOND(9 * 60 * 60 * 1000)
}

interface AverageableMeasurement {
    var time: Date
    var measuredAt
        get() = time.time
        set(value) {
            time = Date(value)
        }
    var value: Double
    var latitude: Double?
    var longitude: Double?
}

interface MeasurementsAveragingHelper {
    suspend fun <T : AverageableMeasurement> averageMeasurements(
        measurements: List<T>,
        startTime: Date,
        averagingWindow: AveragingWindow,
        callback: suspend (T, List<T>) -> Unit
    ): List<T>

    fun calculateAveragingWindow(startTime: Long, lastMeasurement: Long): AveragingWindow
}

class MeasurementsAveragingHelperDefault @Inject constructor() : MeasurementsAveragingHelper {
    private var intervalStart: Long = 0L
    private var intervalEnd: Long = 0L

    override suspend fun <T : AverageableMeasurement> averageMeasurements(
        measurements: List<T>,
        startTime: Date,
        averagingWindow: AveragingWindow,
        callback: suspend (T, List<T>) -> Unit
    ): List<T> {
        intervalStart = startTime.truncateTo(SECOND).time
        intervalEnd = intervalStart + averagingWindow.seconds
        Log.w(TAG, "Initial Interval start: ${Date(intervalStart)}")
        Log.w(TAG, "Initial Interval end: ${Date(intervalEnd)}")

        var measurementsBuffer = mutableListOf<T>()

        measurements.forEach { measurement ->
            if (measurement.measuredAt < intervalStart) return@forEach

            if (measurement.measuredAt >= intervalEnd) {
                // If there are any measurements in the buffer we should average them
                Log.d(TAG, "${Date(measurement.measuredAt)} > ${Date(intervalEnd)}")
                Log.d(TAG, "Measurements buffer: ${measurementsBuffer.map { it.time }}")
                measurementsBuffer.averageMeasurements(intervalEnd - 1000)?.let {
                    callback(it, measurementsBuffer)
                }

                // There can be a long break between measurements. If the current measurement falls outside of the interval we should find the next interval that contains the measurement
                findNextTimeInterval(measurement, averagingWindow)
                measurementsBuffer = mutableListOf(measurement)
                return@forEach
            }

            measurementsBuffer.add(measurement)
        }

        // If the last interval was full then we should average measurements contained in it as well
        if (measurementsBuffer.lastOrNull()?.measuredAt == intervalEnd - 1000) {
            Log.d(TAG, "Buffer is full")
            measurementsBuffer.averageMeasurements(intervalEnd - 1000)?.let {
                callback(it, measurementsBuffer)
            }
            measurementsBuffer.clear()
        }

        return measurementsBuffer
    }

    override fun calculateAveragingWindow(startTime: Long, lastMeasurement: Long): AveragingWindow {
        val sessionDuration = abs(lastMeasurement - startTime)
        return when {
            sessionDuration <= TimeThreshold.FIRST.value -> AveragingWindow.ZERO
            sessionDuration <= TimeThreshold.SECOND.value -> AveragingWindow.FIRST
            else -> AveragingWindow.SECOND
        }
    }

    private fun findNextTimeInterval(
        measurement: AverageableMeasurement,
        averagingWindow: AveragingWindow
    ) {
        if (measurement.measuredAt >= intervalEnd) {
            Log.d(TAG, "Measurement ${measurement.time} > ${Date(intervalEnd)}")
            val timeSinceIntervalStart = measurement.measuredAt - intervalStart
            Log.d(TAG, "Seconds since intervalStart: ${timeSinceIntervalStart / 1000}")
            val remainingSeconds = timeSinceIntervalStart % averagingWindow.seconds
            Log.d(TAG, "remainingSeconds: ${remainingSeconds / 1000}")
            intervalStart = measurement.measuredAt - remainingSeconds
            intervalEnd = intervalStart + averagingWindow.seconds
            Log.w(TAG, "Modified Interval start: ${Date(intervalStart)}")
            Log.w(TAG, "Modified Interval end: ${Date(intervalEnd)}")
        }
    }

    private fun <T : AverageableMeasurement> List<T>.averageMeasurements(time: Long): T? {
        if (isEmpty()) return null

        val average = round(map { it.value }.average())
        val middleIndex = size / 2
        val middleMeasurement = get(middleIndex)
        middleMeasurement.measuredAt = time
        middleMeasurement.value = average
        return middleMeasurement
    }
}
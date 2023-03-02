package pl.llp.aircasting.util.helpers.services

import pl.llp.aircasting.util.extensions.truncateTo
import java.util.*
import java.util.Calendar.SECOND
import javax.inject.Inject
import kotlin.math.abs

class AvgMeasurement(
    val window: AveragingWindow = AveragingWindow.ZERO,
    val threshold: TimeThreshold
)

enum class AveragingWindow(val value: Int) {
    ZERO(1),
    FIRST(5),
    SECOND(60);

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
    fun <T : AverageableMeasurement> averageMeasurements(
        measurements: List<T>,
        startTime: Date,
        averagingWindow: AveragingWindow,
        callback: (T, List<T>) -> Unit
    ): List<T>

    fun calculateAveragingWindow(startTime: Long, lastMeasurement: Long): AveragingWindow
}

class MeasurementsAveragingHelperDefault @Inject constructor() : MeasurementsAveragingHelper {
    private var intervalStart: Long = 0L
    private var intervalEnd: Long = 0L

    override fun <T : AverageableMeasurement> averageMeasurements(
        measurements: List<T>,
        startTime: Date,
        averagingWindow: AveragingWindow,
        callback: (T, List<T>) -> Unit
    ): List<T> {
        intervalStart = startTime.truncateTo(SECOND).time
        intervalEnd = intervalStart + averagingWindow.seconds

        var measurementsBuffer = mutableListOf<T>()

        measurements.forEach { measurement ->
            if (measurement.measuredAt < intervalStart) return@forEach

            if (measurement.measuredAt >= intervalEnd) {
                // If there are any measurements in the buffer we should average them
                measurementsBuffer.averageMeasurements(intervalEnd - 1)?.let {
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
        if (measurementsBuffer.lastOrNull()?.measuredAt == intervalEnd - 1) {
            measurementsBuffer.averageMeasurements(intervalEnd - 1)?.let {
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
            val timeSinceIntervalStart = measurement.measuredAt - intervalStart
            val remainingSeconds =
                (timeSinceIntervalStart / averagingWindow.seconds) % averagingWindow.seconds
            intervalStart = measurement.measuredAt - remainingSeconds
            intervalEnd = intervalStart + averagingWindow.seconds
        }
    }

    private fun <T : AverageableMeasurement> List<T>.averageMeasurements(time: Long): T? {
        if (isEmpty()) return null

        val average = map { it.value }.average()
        val middleIndex = size / 2
        val middleMeasurement = get(middleIndex)
        middleMeasurement.measuredAt = time
        middleMeasurement.value = average
        return middleMeasurement
    }
}
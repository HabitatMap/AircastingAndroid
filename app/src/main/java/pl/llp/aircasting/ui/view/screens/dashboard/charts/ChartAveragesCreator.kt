package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import com.google.common.collect.Lists
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import java.util.*
import kotlin.math.roundToInt

class ChartAveragesCreator {
    companion object {
        const val MAX_AVERAGES_AMOUNT = 9
        private val MOBILE_INTERVAL_IN_SECONDS = 60
        private const val MAX_X_VALUE = 8.0
        private const val MIN_X_VALUE = 0
        private val MOBILE_FREQUENCY_DIVISOR = 8 * 1000.toDouble()
    }

    private var oldEntries: MutableList<Entry> = mutableListOf()
    private var usePreviousEntry = false

    fun getMobileEntries(stream: MeasurementStream): MutableList<Entry> {
        val periodData: MutableList<List<Measurement>?>
        val streamFrequency: Double = stream.samplingFrequency(MOBILE_FREQUENCY_DIVISOR)
        var xValue = MAX_X_VALUE
        val measurementsInPeriod = (MOBILE_INTERVAL_IN_SECONDS / streamFrequency).toInt()
        val entries: MutableList<Entry> = mutableListOf()
        val measurements: MutableList<Measurement>? =
            stream.getMeasurementsForPeriod(MAX_AVERAGES_AMOUNT, MOBILE_FREQUENCY_DIVISOR)

        if (measurementsInPeriod == 0) return mutableListOf()

        periodData = Lists.partition(measurements, measurementsInPeriod)

        if (periodData.size > 0) {
            for (i in periodData.size - 1 downTo 0) {
                var yValue: Double
                try {
                    val dataChunk: List<Measurement> =
                        Collections.synchronizedList(periodData[i])
                    if (dataChunk.size > measurementsInPeriod - getTolerance(
                            measurementsInPeriod.toDouble()
                        )
                    ) {
                        yValue = getAverage(dataChunk).toDouble()
                        if (usePreviousEntry && entries.isNotEmpty()) {
                            yValue = entries[i - 1].y.toDouble()
                        } else if (usePreviousEntry && entries.isEmpty()) {
                            yValue = measurements?.get(0)?.value as Double
                        }
                        usePreviousEntry = false
                        entries.add(
                            Entry(
                                xValue.toFloat(),
                                yValue.toFloat()
                            )
                        )
                        xValue--
                    }
                } catch (e: ConcurrentModificationException) {
                    return oldEntries
                }
            }
        }
        if (entries.size == 0) return entries

        val reversedEntries = Lists.reverse(entries)

        oldEntries = reversedEntries
        return reversedEntries
    }

    fun getMobileEntriesForSessionOverSecondThreshold(lastMeasurements: List<Measurement>): MutableList<Entry> {
        val entries: MutableList<Entry> = mutableListOf()
        var xValue = MAX_X_VALUE
        for (measurement in lastMeasurements.reversed()) {
            entries.add(
                Entry(
                    xValue.toFloat(),
                    measurement.value.toFloat()
                )
            )
            xValue--
        }

        return entries
    }

    fun getFixedEntries(stream: MeasurementStream): MutableList<Entry> {
        val boundary = Calendar.getInstance()
        setMeasurementsAllowedTimeBoundary(stream, boundary)

        val measurements = getMeasurementsAfterAllowedTimeBoundary(stream, boundary)
        var xValue = MIN_X_VALUE
        val entries: MutableList<Entry> = mutableListOf()

        if (measurements.isEmpty()) return entries

        val calendar = Calendar.getInstance()
        val periodData = groupMeasurementsByHours(measurements, calendar)
        if (periodData.isNotEmpty()) {
            // From time to time we still get 10 entries, so this is another check
            val lastNineHoursMeasurementGroups = periodData.entries.toList().takeLast(9)
            for (dataChunk in lastNineHoursMeasurementGroups) {
                if (xValue > MAX_AVERAGES_AMOUNT) return entries

                val yValue = getAverage(dataChunk.value)
                entries.add(
                    Entry(
                        xValue.toFloat(),
                        yValue.toFloat()
                    )
                )
                xValue++
            }
        }
        return entries
    }

    private fun getMeasurementsAfterAllowedTimeBoundary(
        stream: MeasurementStream,
        boundary: Calendar
    ) = stream.measurements.sortedBy { it.time }.filter { it.time > boundary.time }

    private fun setMeasurementsAllowedTimeBoundary(
        stream: MeasurementStream,
        calendar: Calendar
    ) {
        val latestTime = stream.measurements.maxOf { it.time }
        calendar.time = latestTime
        calendar.add(Calendar.HOUR_OF_DAY, -9)
    }

    private fun groupMeasurementsByHours(
        measurements: List<Measurement>,
        calendar: Calendar
    ) = measurements.groupBy {
        calendar.time = it.time
        calendar.get(Calendar.HOUR_OF_DAY)
    }

    private fun getTolerance(measurementsInPeriod: Double): Double {
        return 0.1 * measurementsInPeriod
    }

    private fun getAverage(measurements: List<Measurement>?): Int {
        var sum = 0.0
        var lastIndex = 1
        val m: List<Measurement> = measurements ?: listOf()
        val size = m.size
        try {
            for (i in 0 until size) {
                lastIndex = i
                sum += m[i].value
            }
        } catch (e: ConcurrentModificationException) {
            return if (lastIndex == 0) {
                usePreviousEntry = true
                sum.roundToInt()
            } else {
                sum.roundToInt() / lastIndex
            }
        }
        return ((sum / size).roundToInt())
    }
}

package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import com.google.common.collect.Lists
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import java.util.*
import kotlin.math.roundToInt

open class ChartAveragesCreator {
    companion object {
        const val MAX_AVERAGES_AMOUNT = 9
        const val NUMBER_OF_MEASUREMENTS_IN_ONE_AVERAGE = 60
        private val MOBILE_INTERVAL_IN_SECONDS = 60
        private const val MAX_X_VALUE = 8.0
        const val MIN_X_VALUE = 0
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

    private fun getTolerance(measurementsInPeriod: Double): Double {
        return 0.1 * measurementsInPeriod
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

    open fun getFixedEntries(
        stream: MeasurementStream,
        setStartEndTimeCallback: ((startTime: Date, endTime: Date) -> Unit)? = null
    ): MutableList<Entry> {
        val measurements: MutableList<Measurement>?
        var xValue = MIN_X_VALUE
        val entries: MutableList<Entry> = mutableListOf()

        measurements = stream.getLastMeasurements()

        if (measurements.isEmpty()) return entries

        val periodData = measurements.chunked(NUMBER_OF_MEASUREMENTS_IN_ONE_AVERAGE)

        if (periodData.isNotEmpty()) {
            for (dataChunk in periodData) {
                if (xValue > MAX_AVERAGES_AMOUNT) return entries

                val yValue = getAverage(dataChunk)
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

    protected fun getAverage(measurements: List<Measurement>?): Int {
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

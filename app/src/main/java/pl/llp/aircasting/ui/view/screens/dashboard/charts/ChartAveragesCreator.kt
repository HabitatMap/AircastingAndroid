package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import com.google.common.collect.Lists
import org.apache.commons.lang3.time.DateUtils
import pl.llp.aircasting.data.api.util.Constants
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import java.util.*
import kotlin.math.roundToInt

open class ChartAveragesCreator {
    companion object {
        const val MAX_AVERAGES_AMOUNT = 9
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
        val measurements: MutableList<Measurement> =
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
                            yValue = measurements[0].value
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

    private lateinit var startTimeBoundary: Date
    private lateinit var endTimeBoundary: Date

    fun getFixedEntries(
        stream: MeasurementStream?,
        timeSetterCallback: SessionChartDataCalculator.TimeStampsSetter
    ): MutableList<Entry> {
        if (stream == null || stream.measurements.isEmpty()) return mutableListOf()

        endTimeBoundary = getAllowedEndTimeBoundary(stream)
        startTimeBoundary = getAllowedStartTimeBoundary()

        val measurements = getMeasurementsInAllowedTimeBoundaries(stream)
        var numberOfDots = MIN_X_VALUE
        val entries: MutableList<Entry> = mutableListOf()

        if (measurements.isEmpty()) return entries

        val periodData = groupMeasurementsByHours(measurements)
        if (periodData.isNotEmpty()) {
            // From time to time we still get 10 entries, so this is another check
            val lastNineHoursMeasurementGroups = periodData.entries.toList().takeLast(9)

            val firstEntryDate = getStartDateOfEntries(lastNineHoursMeasurementGroups)
            val lastEntryDate = getEndDateForEntries(lastNineHoursMeasurementGroups)

            for (dataChunk in lastNineHoursMeasurementGroups) {
                if (numberOfDots > MAX_AVERAGES_AMOUNT) return entries

                val currentEntryDate = dataChunk.key

                val yValue = getAverage(dataChunk.value).toFloat()
                val xValue = getXvalueBasedOnTimeDifference(currentEntryDate, firstEntryDate)
                entries.add(
                    Entry(
                        xValue,
                        yValue
                    )
                )
                numberOfDots++
            }

            timeSetterCallback.setStartEndTimeToDisplay(
                modifyHours(firstEntryDate),
                modifyHours(lastEntryDate)
            )
        }
        return entries
    }

    private fun getEndDateForEntries(lastNineHoursMeasurementGroups: List<Map.Entry<Date, List<Measurement>>>) =
        lastNineHoursMeasurementGroups.last().key

    private fun getStartDateOfEntries(lastNineHoursMeasurementGroups: List<Map.Entry<Date, List<Measurement>>>) =
        lastNineHoursMeasurementGroups.first().key

    /* We tweak the hours forward by one, as on UI they are supposed to represent the passed hour
    * So measurements from 6:00:00 till 6:59:59 are represented on UI as 7:00 timestamp
    *  */
    protected open fun modifyHours(date: Date, hours: Int = 1): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR_OF_DAY, hours)
        return calendar.time
    }

    private fun getXvalueBasedOnTimeDifference(
        current: Date,
        first: Date
    ): Float {
        return ((current.time - first.time) / Constants.MILLIS_IN_HOUR).toFloat()
    }

    private fun getMeasurementsInAllowedTimeBoundaries(
        stream: MeasurementStream
    ) = stream.measurements.sortedBy { it.time }.filter {
        it.time in startTimeBoundary..endTimeBoundary
    }

    private fun getAllowedStartTimeBoundary(): Date {
        val calendar = Calendar.getInstance()
        calendar.time = endTimeBoundary
        calendar.add(Calendar.HOUR_OF_DAY, -9)
        return calendar.time
    }

    /*
    * This is used for PurpleAir and all AirBeam sessions.
    * As their last hour of measurements is not complete, we cut off all the measurements from it.
    *  */
    protected open fun getAllowedEndTimeBoundary(stream: MeasurementStream): Date {
        val lastMeasurementTime = stream.measurements.maxOf { it.time }
        val lastMeasurementHour = DateUtils.truncate(lastMeasurementTime, Calendar.HOUR_OF_DAY)
        return Date(lastMeasurementHour.time - 1)
    }

    private fun groupMeasurementsByHours(
        measurements: List<Measurement>,
    ) = measurements.groupBy {
        DateUtils.truncate(it.time, Calendar.HOUR_OF_DAY)
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
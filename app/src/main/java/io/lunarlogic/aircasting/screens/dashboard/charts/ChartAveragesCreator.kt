package io.lunarlogic.aircasting.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import com.google.common.collect.Lists
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import java.util.*
import kotlin.math.roundToInt

class ChartAveragesCreator {
    companion object {
        const val MAX_AVERAGES_AMOUNT = 9
        private val MOBILE_INTERVAL_IN_SECONDS = 60
        private val MAX_X_VALUE = 8
        private val MOBILE_FREQUENCY_DIVISOR = 8 * 1000.toDouble()

    }
    private var oldEntries: MutableList<Entry> = mutableListOf()
    private var usePreviousEntry = false

    fun getMobileEntriesNew(stream: MeasurementStream): MutableList<Entry>? {
        println("MARYSIA: entries calculated the old way: ${getMobileEntries(stream)}")
        var entries: MutableList<Entry>? = mutableListOf()

        val lastMeasurementTime = stream.lastMeasurement().time.time
        val sessionStartTime = stream.firstMeasurement().time.time
        val secondsFromFullMinute = (lastMeasurementTime - sessionStartTime) % 60

        var minuteEnd = lastMeasurementTime
        var minuteStart = minuteEnd - 60 * 1000

        for(i in 8 downTo 0) {
            if ( minuteStart < sessionStartTime) break
            entries?.add(
                Entry(
                    i.toFloat(),
                    averagedValue(stream, minuteStart, minuteEnd)
                ))
            minuteEnd = minuteStart
            minuteStart = minuteEnd - 60 * 1000
        }

        return entries
    }

    private fun averagedValue(stream: MeasurementStream, minuteStart: Long, minuteEnd: Long): Float {

        val timeSpan = Date(minuteStart)..Date(minuteEnd)
        val measurements = stream.getMeasurementsForTimeSpan(timeSpan)
        val sum = measurements.sumByDouble { it.value}
        val avg = sum/measurements.size

        return avg.toFloat()
    }

    fun getMobileEntries(stream: MeasurementStream): MutableList<Entry>? {
        val periodData: MutableList<List<Measurement>?>
        val streamFrequency: Double = stream.samplingFrequency(MOBILE_FREQUENCY_DIVISOR)
        var xValue = MAX_X_VALUE.toDouble()
        val measurementsInPeriod = (MOBILE_INTERVAL_IN_SECONDS / streamFrequency).toInt()
        val entries: MutableList<Entry> = mutableListOf()
        val measurements: MutableList<Measurement>? =
            stream.getMeasurementsForPeriod(MAX_AVERAGES_AMOUNT, MOBILE_FREQUENCY_DIVISOR)

        if (measurementsInPeriod == 0) return mutableListOf()

        periodData = Lists.partition(measurements, measurementsInPeriod)
        val reversedPeriodData: List<List<Measurement>?> =
            Lists.reverse<List<Measurement>?>(periodData)

            if (periodData.size > 0) {
                for (i in reversedPeriodData.indices) {
                    var yValue: Double
                    try {
                        val dataChunk: List<Measurement> =
                            Collections.synchronizedList(reversedPeriodData[i])
                            if (dataChunk.size > measurementsInPeriod - getTolerance(
                                    measurementsInPeriod.toDouble()
                                )
                            ) {
                                yValue = getAverage(dataChunk).toDouble()
                                if (usePreviousEntry && !entries.isEmpty()) {
                                    yValue = entries[i - 1]!!.y.toDouble()
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
        if (entries.size == 0) {
            return entries
        }
        oldEntries = entries
        return entries
    }

    fun getFixedEntries(stream: MeasurementStream): MutableList<Entry>? {
        var measurements: MutableList<Measurement>?
        var xValue = MAX_X_VALUE.toDouble()
        val entries: MutableList<Entry> = mutableListOf()
        val periodData: MutableList<List<Measurement>?> = mutableListOf()
        val maxMeasurementsAmount = 600

        measurements = stream.getLastMeasurements(maxMeasurementsAmount)

        if (measurements == null || measurements.isEmpty()) {
            return entries
        }

        val calendar = Calendar.getInstance()
        calendar.time = measurements[0].time
        var hour: Int = calendar[Calendar.HOUR_OF_DAY]
        var measurementsInHour: MutableList<Measurement> = ArrayList<Measurement>()
        for (i in measurements.indices) {
            val measurement: Measurement = measurements[i]
            calendar.time = measurement.time
            val measurementHour: Int = calendar[Calendar.HOUR_OF_DAY]
            if (hour == measurementHour) {
                measurementsInHour.add(measurement)
            } else {
                periodData.add(measurementsInHour)
                hour = measurementHour
                measurementsInHour = ArrayList<Measurement>()
                measurementsInHour.add(measurement)
            }
        }
        if (periodData.size > 0) {
            for (dataChunk in Lists.reverse<List<Measurement>?>(periodData)) {
                if (xValue < 0) {
                    return entries
                }
                    val yValue = getAverage(dataChunk).toDouble()
                    entries.add(
                        Entry(
                            xValue.toFloat(),
                            yValue.toFloat()
                        )
                    )
                    xValue--
            }
        }
        return if (entries.size == 0) {
            entries
        } else entries
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
                    sum.toInt()
                } else {
                    sum.toInt() / lastIndex
                }
            }
        return (sum / size).toInt()
    }
}

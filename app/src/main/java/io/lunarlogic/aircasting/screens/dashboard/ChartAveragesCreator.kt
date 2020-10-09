package io.lunarlogic.aircasting.screens.dashboard

import com.github.mikephil.charting.data.Entry
import com.google.common.collect.Lists
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class ChartAveragesCreator {
    private val INTERVAL_IN_SECONDS = 60
    private val MAX_AVERAGES_AMOUNT = 9
    private val MAX_X_VALUE = 8
    private val MOBILE_FREQUENCY_DIVISOR = 8 * 1000.toDouble()
    private var oldEntries: List<*> = CopyOnWriteArrayList<Any?>()
    private var usePreviousEntry = false

    @Synchronized
    fun getMobileEntries(stream: MeasurementStream): List<Entry>? {
        val periodData: MutableList<List<Measurement>?>
        val streamFrequency: Double = stream.samplingFrequency(MOBILE_FREQUENCY_DIVISOR)
        var xValue = MAX_X_VALUE.toDouble()
        val measurementsInPeriod = (INTERVAL_IN_SECONDS / streamFrequency).toInt()
        val entries: MutableList<Entry?> = mutableListOf()
        val measurements: MutableList<Measurement>? =
            stream.getMeasurementsForPeriod(MAX_AVERAGES_AMOUNT, MOBILE_FREQUENCY_DIVISOR)
        periodData = Lists.partition(measurements, measurementsInPeriod)
        val reversedPeriodData: List<List<Measurement>?> =
            Lists.reverse<List<Measurement>?>(periodData)
        synchronized(reversedPeriodData) {
            if (periodData.size > 0) {
                for (i in reversedPeriodData.indices) {
                    var yValue: Double
                    try {
                        val dataChunk: List<Measurement> =
                            Collections.synchronizedList(reversedPeriodData[i])
                        synchronized(dataChunk) {
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
                        }
                    } catch (e: ConcurrentModificationException) {
                        return oldEntries
                    }
                }
            }
        }
        if (entries.size == 0) {
            return entries
        }
        oldEntries = entries
        return entries
    }

//    fun getFixedEntries(stream: MeasurementStream): List<Entry?>? {
//        val measurements: MutableList<Measurement>?
//        var xValue = MAX_X_VALUE.toDouble()
//        val entries: MutableList<*> = CopyOnWriteArrayList<Any?>()
//        val periodData: MutableList<List<Measurement>?> = mutableListOf()
//        val maxMeasurementsAmount = 600
//        val measurements = stream.getLastMeasurements(maxMeasurementsAmount)
//
//        if (measurements.isEmpty()) {
//            return entries
//        }
//
//
//        var hour: Int = measurements[0].time.getHours()
//        var measurementsInHour: MutableList<Measurement> = ArrayList<Measurement>()
//        for (i in measurements.indices) {
//            val measurement: Measurement = measurements[i]
//            val measurementHour: Int = measurement.time.getHours()
//            if (hour == measurementHour) {
//                measurementsInHour.add(measurement)
//            } else {
//                periodData.add(measurementsInHour)
//                hour = measurementHour
//                measurementsInHour = ArrayList<Measurement>()
//                measurementsInHour.add(measurement)
//            }
//        }
//        if (periodData.size > 0) {
//            for (dataChunk in Lists.reverse<List<Measurement>?>(periodData)) {
//                if (xValue < 0) {
//                    return entries
//                }
//                synchronized(dataChunk!!) {
//                    val yValue = getAverage(dataChunk).toDouble()
//                    entries.add(
//                        Entry(
//                            xValue.toFloat(),
//                            yValue.toFloat()
//                        )
//                    )
//                    xValue--
//                }
//            }
//        }
//        return if (entries.size == 0) {
//            entries
//        } else entries
//    }

    private fun getTolerance(measurementsInPeriod: Double): Double {
        return 0.1 * measurementsInPeriod
    }

    private fun getAverage(measurements: List<Measurement>?): Int {
        var sum = 0.0
        var lastIndex = 1
        val m: List<Measurement> = Collections.synchronizedList(measurements)
        val size = m.size
        synchronized(m) {
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
        }
        return (sum / size).toInt()
    }
}

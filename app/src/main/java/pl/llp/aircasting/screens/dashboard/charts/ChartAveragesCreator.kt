package pl.llp.aircasting.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import com.google.common.collect.Lists
import pl.llp.aircasting.models.Measurement
import pl.llp.aircasting.models.MeasurementStream
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

    fun getMobileEntries(stream: MeasurementStream): MutableList<Entry> {
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

        oldEntries = entries
        return entries
    }

    fun getMobileEntriesForSessionOverSecondThreshold(lastMeasurements: List<Measurement>): MutableList<Entry> {
        val entries: MutableList<Entry> = mutableListOf()
        var xValue = MAX_X_VALUE.toDouble()
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
        val measurements: MutableList<Measurement>?
        var xValue = MAX_X_VALUE.toDouble()
        val entries: MutableList<Entry> = mutableListOf()
        val periodData: MutableList<List<Measurement>?> = mutableListOf()

        measurements = stream.getLastMeasurements()
        removeMeasurementsFromUnfinishedHour(measurements)

        if (measurements.isEmpty()) return entries

        val calendar = Calendar.getInstance()
        calendar.time = measurements[0].time
        var hour: Int = calendar[Calendar.HOUR_OF_DAY]
        var measurementsInHour: MutableList<Measurement> = ArrayList<Measurement>()
        for (i in measurements.indices) {
            val measurement: Measurement = measurements[i]
            calendar.time = measurement.time
            val measurementHour: Int = calendar[Calendar.HOUR_OF_DAY]
            if (hour == measurementHour) measurementsInHour.add(measurement) else {
                periodData.add(measurementsInHour)
                hour = measurementHour
                measurementsInHour = ArrayList<Measurement>()
                measurementsInHour.add(measurement)
            }
        }
        if (periodData.size > 0) {
            for (dataChunk in periodData) {
                if (xValue < 0) return entries

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
        return entries
    }

    private fun removeMeasurementsFromUnfinishedHour(measurements: MutableList<Measurement>) {
        try {
            measurements.first()
        } catch (e: NoSuchElementException ) {
            return
        }

        val calendar = Calendar.getInstance()
        calendar.time = measurements.first().time

        val unwantedHour = calendar[Calendar.HOUR_OF_DAY]

        while (true) {
            calendar.time = measurements.first().time
            val lastMeasurementHour = calendar[Calendar.HOUR_OF_DAY]
            if (lastMeasurementHour == unwantedHour) {
                measurements.removeFirst()
            } else break
        }
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
        return ((sum / size).roundToInt())
    }
}

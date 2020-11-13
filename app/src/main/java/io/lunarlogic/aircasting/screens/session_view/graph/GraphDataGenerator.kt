package io.lunarlogic.aircasting.screens.session_view.graph

import com.github.mikephil.charting.data.Entry
import io.lunarlogic.aircasting.lib.CalendarUtils
import io.lunarlogic.aircasting.models.Measurement
import java.util.*
import kotlin.collections.ArrayList

class GraphDataGenerator {
    private var cumulativeValue = 0.0
    private var cumulativeTime: Long = 0
    private var count = 0

    private val DEFAULT_LIMIT = 1000

    class Result(val entries: List<Entry>, val midnightPoints: List<Float>)

    fun generate(samples: List<Measurement>, limit: Int = DEFAULT_LIMIT): Result {
        reset()

        val entries = ArrayList<Entry>()
        val midnightPoints = ArrayList<Float>()
        val fillFactor = 1.0 * limit / samples.size
        var fill = 0.0

        val firstMeasurement = samples.firstOrNull()
        firstMeasurement ?: return Result(entries, midnightPoints)
        var lastDateDayOfMonth = CalendarUtils.dayOfMonth(firstMeasurement.time)

        for (measurement in samples) {
            add(measurement)
            fill += fillFactor

            if (fill > 1) {
                fill -= 1.0
                val date = getAverageDate()

                entries.add(buildAverageEntry(date, firstMeasurement.time))

                val dateOfMonth = CalendarUtils.dayOfMonth(date)
                if (lastDateDayOfMonth != dateOfMonth) {
                    lastDateDayOfMonth = dateOfMonth
                    midnightPoints.add(convertDateToFloat(date, firstMeasurement.time))
                }

                reset()
            }
        }

        if (count > 0) {
            val date = getAverageDate()
            entries.add(buildAverageEntry(date, firstMeasurement.time))
        }

        return Result(entries, midnightPoints)
    }

    private fun getAverageDate(): Date {
        return Date(cumulativeTime / count)
    }

    private fun getAverageValue(): Double {
        return (cumulativeValue / count)
    }

    private fun buildAverageEntry(date: Date, startTime: Date): Entry {
        val time = convertDateToFloat(date, startTime)
        val value = getAverageValue().toFloat()
        return Entry(time, value)
    }

    private fun convertDateToFloat(date: Date, startTime: Date): Float {
        // we need to substract startTime because
        // otherwise we lose precision while converting Long to Float
        // and Float is needed for the MPAndroidChart library
        return (date.time - startTime.time).toFloat()
    }

    private fun add(measurement: Measurement) {
        cumulativeValue += measurement.value
        cumulativeTime += measurement.time.time
        count += 1
    }

    private fun reset() {
        count = 0
        cumulativeTime = count.toLong()
        cumulativeValue = cumulativeTime.toDouble()
    }
}

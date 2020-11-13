package io.lunarlogic.aircasting.screens.session_view.graph

import com.github.mikephil.charting.data.Entry
import io.lunarlogic.aircasting.models.Measurement
import java.util.*
import kotlin.collections.ArrayList

class GraphDataGenerator {
    private var cumulativeLongitude = 0.0
    private var cumulativeLatitude = 0.0
    private var cumulativeValue = 0.0
    private var cumulativeTime: Long = 0
    private var count = 0

    private val DEFAULT_LIMIT = 1000

    fun generate(samples: List<Measurement>, limit: Int = DEFAULT_LIMIT): List<Entry> {
        reset()

        val result = ArrayList<Entry>()
        val fillFactor = 1.0 * limit / samples.size
        var fill = 0.0

        val firstMeasurement = samples.firstOrNull()
        firstMeasurement ?: return result

        for (measurement in samples) {
            add(measurement)
            fill += fillFactor

            if (fill > 1) {
                fill -= 1.0
                result.add(buildAverageEntry(firstMeasurement.time))
                reset()
            }
        }

        if (count > 0) {
            result.add(buildAverageEntry(firstMeasurement.time))
        }

        return result
    }

    private fun buildAverageEntry(startTime: Date): Entry {
        val date = Date(cumulativeTime / count)
        // we need to substract startTime because
        // otherwise we lose precision while converting Long to Float
        // and Float is needed for the MPAndroidChart library
        val time = (date.time - startTime.time).toFloat()
        val value = (cumulativeValue / count).toFloat()
        return Entry(time, value)
    }

    private fun add(measurement: Measurement) {
        measurement.latitude?.let { cumulativeLatitude += measurement.latitude }
        measurement.longitude?.let { cumulativeLongitude += measurement.longitude }
        cumulativeValue += measurement.value
        cumulativeTime += measurement.time.time
        count += 1
    }

    private fun reset() {
        count = 0
        cumulativeTime = count.toLong()
        cumulativeValue = cumulativeTime.toDouble()
        cumulativeLatitude = cumulativeValue
        cumulativeLongitude = cumulativeLatitude
    }
}

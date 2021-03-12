package io.lunarlogic.aircasting.lib

import io.lunarlogic.aircasting.models.Measurement

class AveragingService(
    val measurements: List<Measurement>?


) {
    private val WINDOW_SIZE = 5
    private val TRESHOLD = 5 //10 * 60 * 5

    fun averagedMeasurements(): List<Measurement>? {
        var averagedMeasurements: MutableList<Measurement> = mutableListOf()

        if (measurements?.size ?: 0 < TRESHOLD) {
            return measurements
        }
        println("MARYSIA: measurements size: ${measurements?.size}")
        println("MARYSIA: running averaging...")
        measurements?.let {
            it.chunked(WINDOW_SIZE) { measurementsInWindow: List<Measurement> ->
                val middleIndex = measurementsInWindow.size / 2
                val middle = measurementsInWindow[middleIndex]
                val average = measurementsInWindow.sumByDouble { it.value } / measurementsInWindow.size
                val averagedMeasurement = Measurement(average, middle.time, middle.latitude, middle.longitude)
                println("MARYSIA: averaged measurement ${measurementsInWindow.map { "${it.value}}, "} } -> ${average}")
                averagedMeasurements.add(averagedMeasurement)
            }
        }
        println("MARYSIA: averaged measurements size: ${averagedMeasurements.size}")
        return averagedMeasurements
    }
}

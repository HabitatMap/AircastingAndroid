package io.lunarlogic.aircasting.lib

import io.lunarlogic.aircasting.models.Measurement

class AveragingService(
    val measurements: List<Measurement>?

) {
    private val WINDOW_SIZE = 5
    private val TRESHOLD = 60 * 60

    fun averagedMeasurements(): List<Measurement>? {
        if (measurements?.size ?: 0 > TRESHOLD) {
            return measurements?.subList(0, TRESHOLD)
        } else {
            return measurements
        }
    }
}

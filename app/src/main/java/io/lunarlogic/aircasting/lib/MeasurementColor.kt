package io.lunarlogic.aircasting.lib

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.SensorThreshold

class MeasurementColor {
    companion object {
        val LOW_COLOR = R.color.session_color_indicator_low
        val MEDIUM_COLOR = R.color.session_color_indicator_medium
        val HIGH_COLOR = R.color.session_color_indicator_high
        val VERY_HIGH_COLOR = R.color.session_color_indicator_very_high

        private val FALLBACK_COLOR = R.color.aircasting_grey_700
        private val LEVEL_COLORS_IDS = arrayOf(LOW_COLOR, MEDIUM_COLOR, HIGH_COLOR, VERY_HIGH_COLOR)

        class Level(val from: Int, val to: Int, val color: Int)

        fun forMap(context: Context, measurement: Measurement, sensorThreshold: SensorThreshold?): Int {
            sensorThreshold ?: return colorWithResourceId(context, FALLBACK_COLOR)

            val level = measurement.getLevel(sensorThreshold)
            return colorForLevel(context, level)
        }

        fun forMap(context: Context, value: Double?, sensorThreshold: SensorThreshold?): Int {
            value ?: return colorWithResourceId(context, FALLBACK_COLOR)
            sensorThreshold ?: return colorWithResourceId(context, FALLBACK_COLOR)

            val level = Measurement.getLevel(value, sensorThreshold)
            return colorForLevel(context, level)
        }

        fun levels(threshold: SensorThreshold, context: Context): Array<Level> {
            return arrayOf(
                Level(threshold.thresholdVeryLow, threshold.thresholdLow, colorWithResourceId(context, LOW_COLOR)),
                Level(threshold.thresholdLow, threshold.thresholdMedium, colorWithResourceId(context, MEDIUM_COLOR)),
                Level(threshold.thresholdMedium, threshold.thresholdHigh, colorWithResourceId(context, HIGH_COLOR)),
                Level(threshold.thresholdHigh, threshold.thresholdVeryHigh, colorWithResourceId(context, VERY_HIGH_COLOR))
            )
        }

        private fun colorForLevel(context: Context, level: Measurement.Level): Int {
            val colorId = when (level) {
                Measurement.Level.EXTREMELY_LOW -> FALLBACK_COLOR
                Measurement.Level.EXTREMELY_HIGH -> FALLBACK_COLOR
                else -> LEVEL_COLORS_IDS[level.value]
            }

            return colorWithResourceId(context, colorId)
        }

        private fun colorWithResourceId(context: Context, colorId: Int): Int {
            return ResourcesCompat.getColor(context.resources, colorId, null)
        }
    }
}

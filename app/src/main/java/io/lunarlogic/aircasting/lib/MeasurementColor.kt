package io.lunarlogic.aircasting.lib

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.SensorThreshold

class MeasurementColor {
    companion object {
        private val FALLBACK_COLOR = R.color.aircasting_grey_500

        private val LEVEL_COLORS_IDS = arrayOf(
            R.color.session_color_indicator_low,
            R.color.session_color_indicator_medium,
            R.color.session_color_indicator_high,
            R.color.session_color_indicator_very_high
        )

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

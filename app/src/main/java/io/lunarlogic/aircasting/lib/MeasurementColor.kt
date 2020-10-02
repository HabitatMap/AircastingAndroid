package io.lunarlogic.aircasting.lib

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream

class MeasurementColor {
    companion object {
        private val LEVEL_COLORS_IDS = arrayOf(
            R.color.session_color_indicator_low,
            R.color.session_color_indicator_medium,
            R.color.session_color_indicator_high,
            R.color.session_color_indicator_very_high
        )

        fun forMap(context: Context, measurement: Measurement, measurementStream: MeasurementStream): Int {
            val level = measurement.getLevel(measurementStream)
            return colorForLevel(context, level)
        }

        fun forMap(context: Context, value: Double, measurementStream: MeasurementStream): Int {
            val level = Measurement.getLevel(value, measurementStream)
            return colorForLevel(context, level)
        }

        private fun colorForLevel(context: Context, level: Measurement.Level): Int {
            val colorId = when (level) {
                Measurement.Level.EXTREMELY_LOW -> R.color.aircasting_grey_500
                Measurement.Level.EXTREMELY_HIGH -> R.color.aircasting_grey_500
                else -> LEVEL_COLORS_IDS[level.value]
            }

            return ResourcesCompat.getColor(context.resources, colorId, null)
        }
    }
}

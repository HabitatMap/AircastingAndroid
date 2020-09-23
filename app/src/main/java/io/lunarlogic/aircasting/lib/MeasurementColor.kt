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

        fun forTable(context: Context, measurement: Measurement, measurementStream: MeasurementStream): Int? {
            val level = measurement.getLevel(measurementStream)

            val colorId = when (level) {
                Measurement.Level.EXTREMELY_LOW -> null
                Measurement.Level.EXTREMELY_HIGH -> null
                else -> LEVEL_COLORS_IDS[level.value]
            }
            colorId ?: return null

            return ResourcesCompat.getColor(context.resources, colorId, null)
        }

        fun forMap(context: Context, measurement: Measurement, measurementStream: MeasurementStream): Int {
            val level = measurement.getLevel(measurementStream)

            val colorId = when (level) {
                Measurement.Level.EXTREMELY_LOW -> R.color.aircasting_grey_500
                Measurement.Level.EXTREMELY_HIGH -> R.color.session_color_indicator_very_high
                else -> LEVEL_COLORS_IDS[level.value]
            }

            return ResourcesCompat.getColor(context.resources, colorId, null)
        }
    }
}

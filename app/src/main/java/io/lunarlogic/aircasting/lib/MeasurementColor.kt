package io.lunarlogic.aircasting.lib

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import io.lunarlogic.aircasting.R

class MeasurementColor {
    companion object {
        private val LEVEL_COLORS_IDS = arrayOf(
            R.color.session_color_indicator_low,
            R.color.session_color_indicator_medium,
            R.color.session_color_indicator_high,
            R.color.session_color_indicator_very_high
        )

        fun get(context: Context, measurementLevel: Int): Int {
            val colorId = LEVEL_COLORS_IDS[measurementLevel]
            return ResourcesCompat.getColor(context.resources, colorId, null)
        }
    }
}

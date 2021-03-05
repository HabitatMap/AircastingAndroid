package io.lunarlogic.aircasting.screens.session_view

import android.graphics.drawable.GradientDrawable

class StatisticsValueBackground: GradientDrawable {
    private val ALPHA = 30

    constructor(color: Int, radiusCorner: Float = CORNER_RADIUS): super() {
        cornerRadius = radiusCorner
        alpha = ALPHA
        setColor(color)
    }

    companion object {
        val CORNER_RADIUS = 35f
        val RADIUS_BIG = 45F
    }
}

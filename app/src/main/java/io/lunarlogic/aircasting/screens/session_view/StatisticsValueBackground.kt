package io.lunarlogic.aircasting.screens.session_view

import android.graphics.drawable.GradientDrawable

class StatisticsValueBackground: GradientDrawable {
    private val CORNER_RADIUS = 35f
    private val CORNER_RADIUS_NOW = 45f
    private val ALPHA = 30

    constructor(color: Int, isNow: Boolean): super() {
        if (isNow) {
            cornerRadius = CORNER_RADIUS_NOW
        } else {
            cornerRadius = CORNER_RADIUS
        }
        alpha = ALPHA
        setColor(color)
    }
}

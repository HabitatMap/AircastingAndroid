package io.lunarlogic.aircasting.screens.common

import android.graphics.drawable.GradientDrawable

class StatisticsValueBackground: GradientDrawable {
    private val CORNER_RADIUS = 50f
    private val ALPHA = 30

    constructor(color: Int): super() {
        cornerRadius = CORNER_RADIUS
        alpha = ALPHA
        setColor(color)
    }
}

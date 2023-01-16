package pl.llp.aircasting.ui.view.screens.session_view

import android.graphics.drawable.GradientDrawable

class StatisticsValueBackground: GradientDrawable {
    private val ALPHA = 80

    constructor(color: Int, radiusCorner: Float): super() {
        cornerRadius = radiusCorner
        alpha = ALPHA
        setColor(color)
    }

    companion object {
        val CORNER_RADIUS = 35f
        val RADIUS_BIG = 45F
    }
}

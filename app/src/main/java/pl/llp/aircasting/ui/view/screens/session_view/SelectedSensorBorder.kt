package pl.llp.aircasting.ui.view.screens.session_view

import android.graphics.drawable.GradientDrawable

class SelectedSensorBorder(color: Int) : GradientDrawable() {
    private val BORDER_WIDTH = 3
    private val CORNER_RADIUS = 23f

    init {
        setStroke(BORDER_WIDTH, color)
        cornerRadius = CORNER_RADIUS
    }
}

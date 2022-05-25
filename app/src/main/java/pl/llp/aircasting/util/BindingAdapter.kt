package pl.llp.aircasting.util

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.session_view.SelectedSensorBorder
import pl.llp.aircasting.util.SearchHelper.Companion.formatDate
import pl.llp.aircasting.util.SearchHelper.Companion.formatTime

object BindingAdapter {
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter("formatDateStart", "formatDateEnd", requireAll = true)
    fun TextView.setFormatDate(startDateTimeLocal: String, endDateTimeLocal: String) {
        val startDate = formatDate(startDateTimeLocal)
        val startTime = formatTime(startDateTimeLocal)
        val endDate = formatDate(endDateTimeLocal)
        val endTime = formatTime(endDateTimeLocal)

        this.text = "$startDate $startTime - $endDate $endTime"
    }

    @JvmStatic
    @BindingAdapter("setLayoutColors", requireAll = true)
    fun LinearLayout.setLayoutColors(@ColorInt color: Int) {
        if (isSDKGreaterOrEqualToM()) {
            background = SelectedSensorBorder(context.getColor(R.color.aircasting_pink))
            val circle = findViewById<ImageView>(R.id.circle_indicator)
            circle.setColorFilter(context.getColor(R.color.aircasting_pink))
        } else {
            background = SelectedSensorBorder(context.resources.getColor(R.color.aircasting_pink))
            val circle = findViewById<ImageView>(R.id.circle_indicator)
            circle.setColorFilter(context.resources.getColor(R.color.aircasting_pink))
        }
    }

    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter("setSelectedSensorName", requireAll = true)
    fun TextView.setSelectedSensorNameAndType(name: String) {
        val type = this.context.getString(R.string.dashboard_tabs_fixed)
        this.text = "$type, $name"
    }
}
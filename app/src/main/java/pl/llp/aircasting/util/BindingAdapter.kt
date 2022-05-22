package pl.llp.aircasting.util

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.session_view.SelectedSensorBorder
import pl.llp.aircasting.util.SearchHelper.Companion.formatDate
import pl.llp.aircasting.util.SearchHelper.Companion.formatTime

object BindingAdapter {
    @JvmStatic
    @BindingAdapter("setTint", requireAll = true)
    fun ImageView.setImageTint(@ColorInt color: Int) {
        if (isSDKGreaterOrEqualToM()) {
            setColorFilter(this.context.getColor(R.color.aircasting_pink))
        }
//        imageTintList = ColorStateList.valueOf(color)
    }

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
    @BindingAdapter("setSelectedSensorBorderBackgroundDrawable")
    fun View.setSelectedSensorBorderBackgroundDrawable(@ColorInt color: Int) {
        if (isSDKGreaterOrEqualToM()) {
            background = SelectedSensorBorder(this.context.getColor(R.color.aircasting_pink))
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
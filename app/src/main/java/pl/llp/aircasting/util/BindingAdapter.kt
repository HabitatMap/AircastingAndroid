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
    @BindingAdapter("tint")
    fun ImageView.setImageTint(@ColorInt color: Int) {
        setColorFilter(color)
        imageTintList = ColorStateList.valueOf(color)
        Log.i("ImageView", this.toString())
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
}
package pl.llp.aircasting.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import pl.llp.aircasting.util.SearchHelper.Companion.formatDate
import pl.llp.aircasting.util.SearchHelper.Companion.formatTime

object BindingAdapter {

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
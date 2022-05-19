package pl.llp.aircasting.util

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
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
    fun TextView.setFormatDate(startTimeLocal: String, endTimeLocal: String) {
        try {
            val startDate = formatDate(startTimeLocal)
            val startTime = formatTime(startTimeLocal)
            val endDate = formatDate(endTimeLocal)
            val endTime = formatTime(endTimeLocal)

            this.text = "$startDate $startTime - $endDate $endTime"
        } catch (e: Exception) {
            Log.d("TAG", e.message.toString())
        }
    }
}
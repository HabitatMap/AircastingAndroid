package pl.llp.aircasting.util

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.session_view.SelectedSensorBorder
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

    @JvmStatic
    @BindingAdapter("setLayoutColors")
    fun LinearLayout.setLayoutColors(colorData: LiveData<Int>) {
        findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            colorData.observe(lifecycleOwner) {
                val color =
                    if (isSDKGreaterOrEqualToM())
                        context.getColor(it)
                    else context.resources.getColor(it)

                background = SelectedSensorBorder(color)

                val circle = findViewById<ImageView>(R.id.circle_indicator)
                circle.setColorFilter(color)
            }
        }

    }
    // TODO: needs to be revised later.

    @JvmStatic
    @BindingAdapter("setSelectedSensorName", requireAll = true)
    fun TextView.setSelectedSensorNameAndType(name: String) {
        val type = this.context.getString(R.string.dashboard_tabs_fixed)
        this.text = "$type, $name"
    }
}
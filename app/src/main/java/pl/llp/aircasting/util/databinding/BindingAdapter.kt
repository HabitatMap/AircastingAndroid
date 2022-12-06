package pl.llp.aircasting.util.databinding

import android.util.Log
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.button.MaterialButtonToggleGroup
import pl.llp.aircasting.ui.viewmodel.ThresholdAlertFrequency
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

        this.text = "$startDate, $startTime - $endDate, $endTime"
    }

    @BindingAdapter("checkedButton")
    @JvmStatic
    fun setFrequency(view: MaterialButtonToggleGroup, newValue: ThresholdAlertFrequency) {
        @IdRes val checkedButtonId = newValue.buttonId
        Log.v("Frequency", "setFrequency called, buttonId=$checkedButtonId")
        // Important to break potential infinite loops.
        if (view.checkedButtonId != checkedButtonId)
            view.check(checkedButtonId)
    }

    @InverseBindingAdapter(attribute = "checkedButton")
    @JvmStatic
    fun getFrequency(view: MaterialButtonToggleGroup): ThresholdAlertFrequency {
        Log.v("Frequency", "getFrequency called, buttonId=${view.checkedButtonId}")
        return ThresholdAlertFrequency.buildFromButtonId(view.checkedButtonId)
    }

    @BindingAdapter("checkedButtonAttrChanged")
    @JvmStatic fun setListeners(
        view: MaterialButtonToggleGroup,
        attrChange: InverseBindingListener
    ) {
        view.addOnButtonCheckedListener { _, _, _ ->
            attrChange.onChange()
        }
    }
}
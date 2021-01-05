package io.lunarlogic.aircasting.screens.lets_start

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import kotlinx.android.synthetic.main.more_info_bottom_sheet.view.*

class MoreInfoBottomSheet(private val mListener: Listener): BottomSheetDialogFragment() {
    interface Listener {
        fun closePressed()
    }

    private val TAG = "MoreInfoBottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.more_info_bottom_sheet, container, false)
        this.isCancelable = false

        view.close_button.setOnClickListener {
            mListener.closePressed()
        }

        view.description.text = buildDescription()

        return view
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }

    private fun buildDescription(): SpannableStringBuilder {
        val blueColor = context?.let {
            ResourcesCompat.getColor(it.resources, R.color.aircasting_blue_400, null)
        } ?: Color.GRAY

        return SpannableStringBuilder()
            .append(getString(R.string.more_info_text_part1))
            .append(" ")
            .color(blueColor, { bold { append(getString(R.string.more_info_text_part2)) } })
            .append(" ")
            .append(getString(R.string.more_info_text_part3))
            .append(" ")
            .color(blueColor, { bold { append(getString(R.string.more_info_text_part4)) } })
            .append(" ")
            .append(getString(R.string.more_info_text_part5))
    }
}

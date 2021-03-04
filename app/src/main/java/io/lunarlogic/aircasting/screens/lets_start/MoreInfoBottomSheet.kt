package io.lunarlogic.aircasting.screens.lets_start

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import kotlinx.android.synthetic.main.more_info_bottom_sheet.view.*

class MoreInfoBottomSheet(private val mListener: Listener): BottomSheetDialogFragment() {
    interface Listener {
        fun closePressed()
    }

    private val TAG = "MoreInfoBottomSheet"

    var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState)

        val view = View.inflate(getContext(), R.layout.more_info_bottom_sheet, null);

        view.close_button.setOnClickListener {
            mListener.closePressed()
        }

        val card = view.more_info_card
        val params = ConstraintLayout.LayoutParams(card.layoutParams)
        params.height = ((Resources.getSystem().getDisplayMetrics().heightPixels) * 0.9).toInt()
        card?.layoutParams = params

        view.description.text = buildDescription()

        bottomSheet.setContentView(view)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)

        return bottomSheet
    }

    override fun onStart() {
        super.onStart()

        bottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
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

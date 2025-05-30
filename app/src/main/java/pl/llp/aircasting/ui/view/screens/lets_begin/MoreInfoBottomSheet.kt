package pl.llp.aircasting.ui.view.screens.lets_begin

import android.graphics.Color
import android.text.SpannableStringBuilder
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import kotlinx.android.synthetic.main.more_info_bottom_sheet.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BottomSheet

class MoreInfoBottomSheet: BottomSheet() {
    override fun layoutId(): Int {
        return R.layout.more_info_bottom_sheet
    }

    override fun setup() {
        super.setup()
        expandBottomSheet()

        contentView?.description?.text = buildDescription()
    }

    private fun buildDescription(): SpannableStringBuilder {
        val blueColor = context?.let {
            ResourcesCompat.getColor(it.resources, R.color.aircasting_blue_400, null)
        } ?: Color.GRAY

        return SpannableStringBuilder()
            .append(getString(R.string.more_info_text_part1))
            .append(" ")
            .color(blueColor) { bold { append(getString(R.string.more_info_text_part2)) } }
            .append(" ")
            .append(getString(R.string.more_info_text_part3))
            .append(" ")
            .color(blueColor) { bold { append(getString(R.string.more_info_text_part4)) } }
            .append(" ")
            .append(getString(R.string.more_info_text_part5))
    }
}

package pl.llp.aircasting.ui.view.screens.onboarding.measure_and_map

import android.graphics.Color
import android.text.SpannableStringBuilder
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.common.BottomSheet
import kotlinx.android.synthetic.main.learn_more_onboarding_measure_and_map.view.*

class LearnMoreMeasureAndMapBottomSheet: BottomSheet() {
    override fun layoutId(): Int {
        return R.layout.learn_more_onboarding_measure_and_map
    }

    override fun setup() {
        val descriptionViewMobile = contentView?.learn_more_onboarding_measure_and_map_description_mobile
        descriptionViewMobile?.text = buildDescriptionMobile()

        val descriptionViewFixed = contentView?.learn_more_onboarding_measure_and_map_description_fixed
        descriptionViewFixed?.text = buildDescriptionFixed()

        val closeButton = contentView?.close_button
        closeButton?.setOnClickListener {
            dismiss()
        }
    }

    private fun buildDescriptionMobile(): SpannableStringBuilder {
        val greenColor = context?.let {
            ResourcesCompat.getColor(it.resources, R.color.aircasting_green, null)
        } ?: Color.GRAY

        return SpannableStringBuilder()
            .append(getString(R.string.onboarding_page3_description1_part1))
            .append(" ")
            .color(greenColor, { bold { append(getString(R.string.onboarding_page3_description1_part2)) } })
            .append(" ")
            .append(getString(R.string.onboarding_page3_description1_part3))
    }

    private fun buildDescriptionFixed(): SpannableStringBuilder {
        val greenColor = context?.let {
            ResourcesCompat.getColor(it.resources, R.color.aircasting_green, null)
        } ?: Color.GRAY

        return SpannableStringBuilder()
            .append(getString(R.string.onboarding_page3_description2_part1))
            .append(" ")
            .color(greenColor, { bold { append(getString(R.string.onboarding_page3_description2_part2)) } })
            .append(" ")
            .append(getString(R.string.onboarding_page3_description2_part3))
    }

}

package io.lunarlogic.aircasting.screens.onboarding.measure_and_map

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R

class LearnMoreMeasureAndMapBottomSheet: BottomSheetDialogFragment() {

    private val TAG = "LearnMorePage3BottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.learn_more_onboarding_page3, container, false)

        val descriptionViewMobile = view?.findViewById<TextView>(R.id.learn_more_onboarding_page3_description_mobile)
        descriptionViewMobile?.text = buildDescriptionMobile()

        val descriptionViewFixed = view?.findViewById<TextView>(R.id.learn_more_onboarding_page3_description_fixed)
        descriptionViewFixed?.text = buildDescriptionFixed()

        val closeButton = view?.findViewById<ImageView>(R.id.close_button)
        closeButton?.setOnClickListener {
            dismiss()
        }

        return view
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }

    fun buildDescriptionMobile(): SpannableStringBuilder {
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

    fun buildDescriptionFixed(): SpannableStringBuilder {
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

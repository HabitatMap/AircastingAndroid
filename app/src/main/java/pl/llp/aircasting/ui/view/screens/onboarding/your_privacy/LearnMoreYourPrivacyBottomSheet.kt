package pl.llp.aircasting.ui.view.screens.onboarding.your_privacy

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BottomSheet
import kotlinx.android.synthetic.main.learn_more_onboarding_your_privacy.view.*

class LearnMoreYourPrivacyBottomSheet: BottomSheet() {
    override fun layoutId(): Int {
        return R.layout.learn_more_onboarding_your_privacy
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState)

        contentView?.learn_more_onboarding_your_privacy_description?.text = buildDescription()

        val closeButton = contentView?.close_button
        closeButton?.setOnClickListener {
            dismiss()
        }

        expandBottomSheet()

        return bottomSheet
    }

    private fun buildDescription(): SpannableStringBuilder {
        return SpannableStringBuilder()
            .append(getString(R.string.onboarding_bottomsheet_page4_paragraph_header))
            .append("\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_1)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_1))
            .append("\n\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_2)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_2))
            .append("\n\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_3)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_3))
            .append("\n\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_4)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_4))
            .append("\n\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_5)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_5))
            .append("\n\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_6)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_6))
            .append("\n\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_7)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_7))
            .append("\n\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_8)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_8))
            .append("\n\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_9)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_9))
            .append("\n\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_10)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_10))
            .append("\n\n")
            .append(buildHeader(getString(R.string.onboarding_bottomsheet_page4_paragraph_11)))
            .append("\n")
            .append(getString(R.string.onboarding_bottomsheet_page4_description_11))
            .append("\n\n")
    }

    private fun buildHeader(header: String): SpannableString {
        val spannableString = SpannableString(header)
        val boldSpan = StyleSpan(Typeface.BOLD)
        val absoluteSizeSpan = AbsoluteSizeSpan(24, true) // 24 is the right font size
        spannableString.setSpan(boldSpan, 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(absoluteSizeSpan, 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }
}

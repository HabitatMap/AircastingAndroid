package io.lunarlogic.aircasting.screens.onboarding.your_privacy

import android.app.Dialog
import android.os.Bundle
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BottomSheet
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

    private fun buildDescription(): String {
        return getString(R.string.onboarding_bottomsheet_page4_description1) + "\n\n" + getString(R.string.onboarding_bottomsheet_page4_description2)
    }
}

package pl.llp.aircasting.ui.view.screens.new_session.session_details

import android.text.SpannableStringBuilder
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.onboarding.your_privacy.LearnMoreYourPrivacyBottomSheet

class WeDoNotLeakPasswordsBottomSheet : LearnMoreYourPrivacyBottomSheet() {
    override val EXPANDED_PERCENT = 1.0

    override fun layoutId(): Int {
        return R.layout.we_do_not_leak_passwords
    }

    override fun buildDescription(): SpannableStringBuilder {
        return SpannableStringBuilder()
            .append(getString(R.string.we_do_not_leak_passwords))
    }
}

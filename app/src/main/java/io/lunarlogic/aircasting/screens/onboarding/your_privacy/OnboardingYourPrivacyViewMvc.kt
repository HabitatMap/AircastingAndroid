package io.lunarlogic.aircasting.screens.onboarding.your_privacy

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface OnboardingYourPrivacyViewMvc: ObservableViewMvc<OnboardingYourPrivacyViewMvc.Listener> {
    interface Listener {
        fun onAcceptClicked()
        fun onLearnMorePage4Clicked()
    }
}

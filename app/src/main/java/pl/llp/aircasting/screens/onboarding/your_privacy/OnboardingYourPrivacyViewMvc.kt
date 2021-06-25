package pl.llp.aircasting.screens.onboarding.your_privacy

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface OnboardingYourPrivacyViewMvc: ObservableViewMvc<OnboardingYourPrivacyViewMvc.Listener> {
    interface Listener {
        fun onAcceptClicked()
        fun onLearnMoreYourPrivacyClicked()
    }
}

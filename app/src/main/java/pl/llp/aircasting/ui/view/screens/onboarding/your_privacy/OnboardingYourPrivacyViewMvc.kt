package pl.llp.aircasting.ui.view.screens.onboarding.your_privacy

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface OnboardingYourPrivacyViewMvc: ObservableViewMvc<OnboardingYourPrivacyViewMvc.Listener> {
    interface Listener {
        fun onAcceptClicked()
        fun onLearnMoreYourPrivacyClicked()
    }
}

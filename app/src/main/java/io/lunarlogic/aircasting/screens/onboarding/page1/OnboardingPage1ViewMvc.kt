package io.lunarlogic.aircasting.screens.onboarding.page1

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface OnboardingPage1ViewMvc: ObservableViewMvc<OnboardingPage1ViewMvc.Listener> {
    interface Listener {
        fun onGetStartedClicked()
    }
}

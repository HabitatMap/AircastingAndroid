package io.lunarlogic.aircasting.screens.onboarding.page3

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface OnboardingPage3ViewMvc: ObservableViewMvc<OnboardingPage3ViewMvc.Listener> {
    interface Listener {
        fun onContinuePage3Clicked()
        fun onLearnMorePage3Clicked()
    }
}

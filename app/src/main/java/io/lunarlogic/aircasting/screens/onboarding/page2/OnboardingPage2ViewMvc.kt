package io.lunarlogic.aircasting.screens.onboarding.page2

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface OnboardingPage2ViewMvc: ObservableViewMvc<OnboardingPage2ViewMvc.Listener> {
    interface Listener {
        fun onContinuePage2Clicked()
    }
}

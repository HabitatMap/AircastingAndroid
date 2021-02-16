package io.lunarlogic.aircasting.screens.onboarding.page4

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface OnboardingPage4ViewMvc: ObservableViewMvc<OnboardingPage4ViewMvc.Listener> {
    interface Listener {
        fun onAcceptClicked()
        fun onLearnMorePage4Clicked()
    }
}

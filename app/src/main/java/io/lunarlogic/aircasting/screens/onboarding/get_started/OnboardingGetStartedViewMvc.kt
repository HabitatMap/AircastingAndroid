package io.lunarlogic.aircasting.screens.onboarding.get_started

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface OnboardingGetStartedViewMvc: ObservableViewMvc<OnboardingGetStartedViewMvc.Listener> {
    interface Listener {
        fun onGetStartedClicked()
    }
}

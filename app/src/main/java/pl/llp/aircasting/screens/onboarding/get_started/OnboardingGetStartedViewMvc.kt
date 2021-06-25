package pl.llp.aircasting.screens.onboarding.get_started

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface OnboardingGetStartedViewMvc: ObservableViewMvc<OnboardingGetStartedViewMvc.Listener> {
    interface Listener {
        fun onGetStartedClicked()
    }
}

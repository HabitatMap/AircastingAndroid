package pl.llp.aircasting.ui.view.screens.onboarding.get_started

import pl.llp.aircasting.ui.view.screens.common.ObservableViewMvc

interface OnboardingGetStartedViewMvc: ObservableViewMvc<OnboardingGetStartedViewMvc.Listener> {
    interface Listener {
        fun onGetStartedClicked()
    }
}

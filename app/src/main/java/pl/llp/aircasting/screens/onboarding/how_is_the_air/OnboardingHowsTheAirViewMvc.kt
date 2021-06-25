package pl.llp.aircasting.screens.onboarding.how_is_the_air

import pl.llp.aircasting.screens.common.ObservableViewMvc


interface OnboardingHowsTheAirViewMvc: ObservableViewMvc<OnboardingHowsTheAirViewMvc.Listener> {
    interface Listener {
        fun onContinueHowsTheAirClicked()
    }
}

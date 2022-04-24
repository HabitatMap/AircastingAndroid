package pl.llp.aircasting.ui.view.screens.onboarding.how_is_the_air

import pl.llp.aircasting.ui.view.screens.common.ObservableViewMvc


interface OnboardingHowsTheAirViewMvc: ObservableViewMvc<OnboardingHowsTheAirViewMvc.Listener> {
    interface Listener {
        fun onContinueHowsTheAirClicked()
    }
}

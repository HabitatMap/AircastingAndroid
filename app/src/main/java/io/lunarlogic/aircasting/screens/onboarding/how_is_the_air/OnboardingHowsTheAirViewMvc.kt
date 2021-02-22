package io.lunarlogic.aircasting.screens.onboarding.how_is_the_air

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc


interface OnboardingHowsTheAirViewMvc: ObservableViewMvc<OnboardingHowsTheAirViewMvc.Listener> {
    interface Listener {
        fun onContinueHowsTheAirClicked()
    }
}

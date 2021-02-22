package io.lunarlogic.aircasting.screens.onboarding.measure_and_map

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface OnboardingMeasureAndMapViewMvc: ObservableViewMvc<OnboardingMeasureAndMapViewMvc.Listener> {
    interface Listener {
        fun onContinuePage3Clicked()
        fun onLearnMorePage3Clicked()
    }
}

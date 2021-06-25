package pl.llp.aircasting.screens.onboarding.measure_and_map

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface OnboardingMeasureAndMapViewMvc: ObservableViewMvc<OnboardingMeasureAndMapViewMvc.Listener> {
    interface Listener {
        fun onContinueMeasureAndMapClicked()
        fun onLearnMoreMeasureAndMapClicked()
    }
}

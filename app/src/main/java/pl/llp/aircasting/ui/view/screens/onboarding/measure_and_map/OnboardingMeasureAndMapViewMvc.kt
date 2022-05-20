package pl.llp.aircasting.ui.view.screens.onboarding.measure_and_map

import pl.llp.aircasting.ui.view.common.ObservableViewMvc

interface OnboardingMeasureAndMapViewMvc: ObservableViewMvc<OnboardingMeasureAndMapViewMvc.Listener> {
    interface Listener {
        fun onContinueMeasureAndMapClicked()
        fun onLearnMoreMeasureAndMapClicked()
    }
}

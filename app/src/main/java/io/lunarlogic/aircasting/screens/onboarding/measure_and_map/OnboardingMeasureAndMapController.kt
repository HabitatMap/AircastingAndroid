package io.lunarlogic.aircasting.screens.onboarding.measure_and_map


class OnboardingMeasureAndMapController(
    private val mViewMvc: OnboardingMeasureAndMapViewMvc
) {

    fun registerListener(listener: OnboardingMeasureAndMapViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingMeasureAndMapViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

}

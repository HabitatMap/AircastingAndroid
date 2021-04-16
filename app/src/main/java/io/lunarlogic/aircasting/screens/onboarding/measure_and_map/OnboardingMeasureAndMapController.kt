package io.lunarlogic.aircasting.screens.onboarding.measure_and_map

import io.lunarlogic.aircasting.screens.common.BaseController

class OnboardingMeasureAndMapController(
    var viewMvc: OnboardingMeasureAndMapViewMvcImpl?
) :  BaseController<OnboardingMeasureAndMapViewMvcImpl>(viewMvc) {

    fun registerListener(listener: OnboardingMeasureAndMapViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingMeasureAndMapViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

}

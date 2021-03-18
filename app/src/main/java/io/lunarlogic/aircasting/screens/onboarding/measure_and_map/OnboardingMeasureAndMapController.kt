package io.lunarlogic.aircasting.screens.onboarding.measure_and_map

import io.lunarlogic.aircasting.screens.common.BaseController


class OnboardingMeasureAndMapController(
    private var mViewMvc: OnboardingMeasureAndMapViewMvc?
) : BaseController(mView = mViewMvc) {

    fun registerListener(listener: OnboardingMeasureAndMapViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingMeasureAndMapViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

}

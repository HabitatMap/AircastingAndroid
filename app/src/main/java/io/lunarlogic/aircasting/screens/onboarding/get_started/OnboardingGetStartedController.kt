package io.lunarlogic.aircasting.screens.onboarding.get_started

import io.lunarlogic.aircasting.screens.common.BaseController


class OnboardingGetStartedController(
    private var viewMvc: OnboardingGetStartedViewMvcImpl?
) : BaseController<OnboardingGetStartedViewMvcImpl>(viewMvc){

    fun registerListener(listener: OnboardingGetStartedViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingGetStartedViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}

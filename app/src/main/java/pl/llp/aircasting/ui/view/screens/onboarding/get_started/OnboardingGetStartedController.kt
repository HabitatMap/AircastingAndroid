package pl.llp.aircasting.ui.view.screens.onboarding.get_started

import pl.llp.aircasting.ui.view.common.BaseController


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

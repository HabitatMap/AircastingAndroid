package pl.llp.aircasting.ui.view.screens.onboarding.how_is_the_air

import pl.llp.aircasting.ui.view.common.BaseController


class OnboardingHowsTheAirController(
    var viewMvc: OnboardingHowsTheAirViewMvcImpl?
) : BaseController<OnboardingHowsTheAirViewMvcImpl>(viewMvc){

    fun registerListener(listener: OnboardingHowsTheAirViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingHowsTheAirViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}

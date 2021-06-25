package pl.llp.aircasting.screens.onboarding.your_privacy

import pl.llp.aircasting.screens.common.BaseController

class OnboardingYourPrivacyController(
    var viewMvc: OnboardingYourPrivacyViewMvcImpl?
) :  BaseController<OnboardingYourPrivacyViewMvcImpl>(viewMvc) {

    fun registerListener(listener: OnboardingYourPrivacyViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingYourPrivacyViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}

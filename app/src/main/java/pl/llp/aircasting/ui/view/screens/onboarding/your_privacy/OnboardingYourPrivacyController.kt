package pl.llp.aircasting.ui.view.screens.onboarding.your_privacy

import pl.llp.aircasting.ui.view.common.BaseController

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

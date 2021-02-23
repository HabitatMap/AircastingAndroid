package io.lunarlogic.aircasting.screens.onboarding.your_privacy


class OnboardingYourPrivacyController(
    private val mViewMvc: OnboardingYourPrivacyViewMvc
) {

    fun registerListener(listener: OnboardingYourPrivacyViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingYourPrivacyViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

}

package io.lunarlogic.aircasting.screens.onboarding.page3


class OnboardingPage3Controller(
    private val mViewMvc: OnboardingPage3ViewMvc
) {

    fun registerListener(listener: OnboardingPage3ViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingPage3ViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

}

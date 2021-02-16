package io.lunarlogic.aircasting.screens.onboarding.page4


class OnboardingPage4Controller(
    private val mViewMvc: OnboardingPage4ViewMvc
) {

    fun registerListener(listener: OnboardingPage4ViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingPage4ViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

}

package io.lunarlogic.aircasting.screens.onboarding.page3


class OnboardingPage3Controller(
    private val mViewMvc: OnboardingPage3ViewMvc
): OnboardingPage3ViewMvc.Listener {

    fun registerListener(listener: OnboardingPage3ViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingPage3ViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

    override fun onContinuePage3Clicked() {
        TODO("Not yet implemented")
    }

    override fun onLearnMorePage3Clicked() {
        TODO("Not yet implemented")
    }
}

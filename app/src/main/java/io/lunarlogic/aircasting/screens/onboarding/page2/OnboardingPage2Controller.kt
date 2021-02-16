package io.lunarlogic.aircasting.screens.onboarding.page2


class OnboardingPage2Controller(
    private val mViewMvc: OnboardingPage2ViewMvc
): OnboardingPage2ViewMvc.Listener {

    fun registerListener(listener: OnboardingPage2ViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingPage2ViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

    override fun onContinuePage2Clicked() {
        TODO("Not yet implemented")
    }
}

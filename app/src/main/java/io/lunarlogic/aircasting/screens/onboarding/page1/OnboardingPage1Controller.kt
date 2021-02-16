package io.lunarlogic.aircasting.screens.onboarding.page1


class OnboardingPage1Controller(
    private val mViewMvc: OnboardingPage1ViewMvc
): OnboardingPage1ViewMvc.Listener {

    fun registerListener(listener: OnboardingPage1ViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingPage1ViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

    override fun onGetStartedClicked() {
        TODO("Not yet implemented")
    }
}

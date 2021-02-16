package io.lunarlogic.aircasting.screens.onboarding.page4


class OnboardingPage4Controller(
    private val mViewMvc: OnboardingPage4ViewMvc
): OnboardingPage4ViewMvc.Listener {

    fun registerListener(listener: OnboardingPage4ViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingPage4ViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

    override fun onAcceptClicked() {
        TODO("Not yet implemented")
    }

    override fun onLearnMorePage4Clicked() {
        TODO("Not yet implemented")
    }
}

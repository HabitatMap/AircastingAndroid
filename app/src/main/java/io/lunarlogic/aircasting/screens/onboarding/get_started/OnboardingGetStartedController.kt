package io.lunarlogic.aircasting.screens.onboarding.get_started


class OnboardingGetStartedController(
    private var mViewMvc: OnboardingGetStartedViewMvc?
) {

    fun registerListener(listener: OnboardingGetStartedViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingGetStartedViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

    fun onDestroy() {
        mViewMvc = null
    }
}

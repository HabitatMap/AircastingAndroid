package io.lunarlogic.aircasting.screens.onboarding.how_is_the_air


class OnboardingHowsTheAirController(
    private val mViewMvc: OnboardingHowsTheAirViewMvc
){

    fun registerListener(listener: OnboardingHowsTheAirViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingHowsTheAirViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }

}

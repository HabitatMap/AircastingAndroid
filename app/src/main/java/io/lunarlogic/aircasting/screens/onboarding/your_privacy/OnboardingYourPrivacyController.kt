package io.lunarlogic.aircasting.screens.onboarding.your_privacy

import io.lunarlogic.aircasting.screens.common.BaseController


class OnboardingYourPrivacyController(
    private var mViewMvc: OnboardingYourPrivacyViewMvc?
) : BaseController(mView = mViewMvc){

    fun registerListener(listener: OnboardingYourPrivacyViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: OnboardingYourPrivacyViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}

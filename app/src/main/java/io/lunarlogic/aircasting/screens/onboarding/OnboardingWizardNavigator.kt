package io.lunarlogic.aircasting.screens.onboarding

import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.common.BaseWizardNavigator
import io.lunarlogic.aircasting.screens.onboarding.get_started.OnboardingGetStartedFragment
import io.lunarlogic.aircasting.screens.onboarding.get_started.OnboardingGetStartedViewMvc
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirFragment
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvc
import io.lunarlogic.aircasting.screens.onboarding.measure_and_map.OnboardingMeasureAndMapFragment
import io.lunarlogic.aircasting.screens.onboarding.measure_and_map.OnboardingMeasureAndMapViewMvc
import io.lunarlogic.aircasting.screens.onboarding.your_privacy.OnboardingYourPrivacyFragment
import io.lunarlogic.aircasting.screens.onboarding.your_privacy.OnboardingYourPrivacyViewMvc

class OnboardingWizardNavigator(
    private val mViewMvc: OnboardingViewMvc,
    private val mFragmentManager: FragmentManager
): BaseWizardNavigator(mViewMvc, mFragmentManager) {

    fun goToStep1(listener: OnboardingGetStartedViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingGetStartedFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToStep2(listener: OnboardingHowsTheAirViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingHowsTheAirFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToStep3(listener: OnboardingMeasureAndMapViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingMeasureAndMapFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToStep4(listener: OnboardingYourPrivacyViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingYourPrivacyFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }
}

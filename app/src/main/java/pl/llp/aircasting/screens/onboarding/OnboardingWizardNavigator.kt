package pl.llp.aircasting.screens.onboarding

import android.widget.ProgressBar
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.ProgressBarCounter
import pl.llp.aircasting.screens.common.BaseWizardNavigator
import pl.llp.aircasting.screens.onboarding.get_started.OnboardingGetStartedFragment
import pl.llp.aircasting.screens.onboarding.get_started.OnboardingGetStartedViewMvc
import pl.llp.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirFragment
import pl.llp.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvc
import pl.llp.aircasting.screens.onboarding.measure_and_map.OnboardingMeasureAndMapFragment
import pl.llp.aircasting.screens.onboarding.measure_and_map.OnboardingMeasureAndMapViewMvc
import pl.llp.aircasting.screens.onboarding.your_privacy.OnboardingYourPrivacyFragment
import pl.llp.aircasting.screens.onboarding.your_privacy.OnboardingYourPrivacyViewMvc

class OnboardingWizardNavigator(
    private val mViewMvc: OnboardingViewMvc,
    private val mFragmentManager: FragmentManager
): BaseWizardNavigator(mViewMvc, mFragmentManager, R.id.onboarding_fragment_container) {
    override val STEP_PROGRESS = 15

    fun goToGetStarted(listener: OnboardingGetStartedViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingGetStartedFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToHowIsTheAir(listener: OnboardingHowsTheAirViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingHowsTheAirFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToMeasureAndMap(listener: OnboardingMeasureAndMapViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingMeasureAndMapFragment()
        fragment.listener = listener
        mViewMvc.changeProgressBarColorToGreen()
        goToFragment(fragment)
    }

    fun goToYourPrivacy(listener: OnboardingYourPrivacyViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingYourPrivacyFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun setupProgressBarMax() {
        progressBarCounter.currentProgressMax = ProgressBarCounter.DEFAULT_ONBOARDING_STEP_NUMBER * STEP_PROGRESS
        val progressBar = mViewMvc.rootView?.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar?.max = progressBarCounter.currentProgressMax
    }
}

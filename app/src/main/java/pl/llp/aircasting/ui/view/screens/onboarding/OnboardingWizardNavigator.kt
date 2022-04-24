package pl.llp.aircasting.ui.view.screens.onboarding

import android.widget.ProgressBar
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.util.ProgressBarCounter
import pl.llp.aircasting.ui.view.screens.common.BaseWizardNavigator
import pl.llp.aircasting.ui.view.screens.onboarding.get_started.OnboardingGetStartedFragment
import pl.llp.aircasting.ui.view.screens.onboarding.get_started.OnboardingGetStartedViewMvc
import pl.llp.aircasting.ui.view.screens.onboarding.how_is_the_air.OnboardingHowsTheAirFragment
import pl.llp.aircasting.ui.view.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvc
import pl.llp.aircasting.ui.view.screens.onboarding.measure_and_map.OnboardingMeasureAndMapFragment
import pl.llp.aircasting.ui.view.screens.onboarding.measure_and_map.OnboardingMeasureAndMapViewMvc
import pl.llp.aircasting.ui.view.screens.onboarding.your_privacy.OnboardingYourPrivacyFragment
import pl.llp.aircasting.ui.view.screens.onboarding.your_privacy.OnboardingYourPrivacyViewMvc

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

    // TODO: Below method will be needed when implementing ticket:
    // https://trello.com/c/hjkuOowK/1350-intro-wizard-unhide-hows-the-air-near-you (after "following & let's begin: search & follow fixed sessions")
    fun goToHowIsTheAir(listener: OnboardingHowsTheAirViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingHowsTheAirFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToMeasureandMap(listener: OnboardingMeasureAndMapViewMvc.Listener) {
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

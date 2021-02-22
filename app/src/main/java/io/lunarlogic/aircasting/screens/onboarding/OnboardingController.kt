package io.lunarlogic.aircasting.screens.onboarding

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.create_account.CreateAccountActivity
import io.lunarlogic.aircasting.screens.onboarding.get_started.OnboardingGetStartedViewMvc
import io.lunarlogic.aircasting.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvc
import io.lunarlogic.aircasting.screens.onboarding.measure_and_map.LearnMoreMeasureAndMapBottomSheet
import io.lunarlogic.aircasting.screens.onboarding.measure_and_map.OnboardingMeasureAndMapViewMvc
import io.lunarlogic.aircasting.screens.onboarding.your_privacy.LearnMoreYourPrivacyBottomSheet
import io.lunarlogic.aircasting.screens.onboarding.your_privacy.OnboardingYourPrivacyViewMvc

class OnboardingController(
    private val mContextActivity: AppCompatActivity,
    mViewMvc: OnboardingViewMvc,
    private val mFragmentManager: FragmentManager,
    private val mSettings: Settings
): OnboardingGetStartedViewMvc.Listener,
    OnboardingHowsTheAirViewMvc.Listener,
    OnboardingMeasureAndMapViewMvc.Listener,
    OnboardingYourPrivacyViewMvc.Listener {
    private val wizardNavigator = OnboardingWizardNavigator(mViewMvc, mFragmentManager)

    fun onBackPressed() {
        wizardNavigator.onBackPressed()
    }

    fun onCreate() {
        wizardNavigator.goToStep1(this)
    }

    override fun onGetStartedClicked() {
        wizardNavigator.goToStep2(this)
    }

    override fun onContinueHowsTheAirClicked() {
        wizardNavigator.goToStep3(this)
    }

    override fun onContinueMeasureAndMapClicked() {
        wizardNavigator.goToStep4(this)
    }

    override fun onLearnMoreMeasureAndMapClicked() {
        val bottomsheet = LearnMoreMeasureAndMapBottomSheet()
        bottomsheet.show(mFragmentManager)
    }

    override fun onAcceptClicked() {
        CreateAccountActivity.start(mContextActivity) //todo: maybe i should do it from wizard navigator <?>
    }

    override fun onLearnMoreYourPrivacyClicked() {
        val bottomsheet = LearnMoreYourPrivacyBottomSheet()
        bottomsheet.show(mFragmentManager)
    }

}

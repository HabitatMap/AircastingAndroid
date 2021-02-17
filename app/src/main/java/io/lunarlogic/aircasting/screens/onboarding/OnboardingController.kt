package io.lunarlogic.aircasting.screens.onboarding

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.create_account.CreateAccountActivity
import io.lunarlogic.aircasting.screens.onboarding.page1.OnboardingPage1ViewMvc
import io.lunarlogic.aircasting.screens.onboarding.page2.OnboardingPage2ViewMvc
import io.lunarlogic.aircasting.screens.onboarding.page3.LearnMorePage3BottomSheet
import io.lunarlogic.aircasting.screens.onboarding.page3.OnboardingPage3ViewMvc
import io.lunarlogic.aircasting.screens.onboarding.page4.LearnMorePage4BottomSheet
import io.lunarlogic.aircasting.screens.onboarding.page4.OnboardingPage4ViewMvc

class OnboardingController(
    private val mContextActivity: AppCompatActivity,
    mViewMvc: OnboardingViewMvc,
    private val mFragmentManager: FragmentManager,
    private val mSettings: Settings
): OnboardingPage1ViewMvc.Listener,
    OnboardingPage2ViewMvc.Listener,
    OnboardingPage3ViewMvc.Listener,
    OnboardingPage4ViewMvc.Listener {
    private val wizardNavigator = OnboardingWizardNavigator(mViewMvc, mFragmentManager)

    fun onBackPressed() {
        wizardNavigator.onBackPressed()
    }

    fun onCreate() {
        wizardNavigator.goToPage1(this)
    }

    override fun onGetStartedClicked() {
        wizardNavigator.goToPage2(this)
    }

    override fun onContinuePage2Clicked() {
        wizardNavigator.goToPage3(this)
    }

    override fun onContinuePage3Clicked() {
        wizardNavigator.goToPage4(this)
    }

    override fun onLearnMorePage3Clicked() {
        val bottomsheet = LearnMorePage3BottomSheet()
        bottomsheet.show(mFragmentManager)
    }

    override fun onAcceptClicked() {
        CreateAccountActivity.start(mContextActivity) //todo: maybe i should do it from wizard navigator <?>
    }

    override fun onLearnMorePage4Clicked() {
        val bottomsheet = LearnMorePage4BottomSheet()
        bottomsheet.show(mFragmentManager)
    }

}

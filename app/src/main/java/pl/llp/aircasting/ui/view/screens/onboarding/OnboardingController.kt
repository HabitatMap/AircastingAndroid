package pl.llp.aircasting.ui.view.screens.onboarding

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.ui.view.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.ui.view.fragments.onboarding.OnboardingGetStartedFragment
import pl.llp.aircasting.ui.view.screens.onboarding.get_started.OnboardingGetStartedViewMvc
import pl.llp.aircasting.ui.view.screens.onboarding.how_is_the_air.OnboardingHowsTheAirViewMvc
import pl.llp.aircasting.ui.view.screens.onboarding.measure_and_map.LearnMoreMeasureAndMapBottomSheet
import pl.llp.aircasting.ui.view.fragments.onboarding.OnboardingMeasureAndMapFragment
import pl.llp.aircasting.ui.view.screens.onboarding.measure_and_map.OnboardingMeasureAndMapViewMvc
import pl.llp.aircasting.ui.view.screens.onboarding.your_privacy.LearnMoreYourPrivacyBottomSheet
import pl.llp.aircasting.ui.view.screens.onboarding.your_privacy.OnboardingYourPrivacyViewMvc
import pl.llp.aircasting.util.Settings

class OnboardingController(
    private val mContextActivity: AppCompatActivity,
    private val mViewMvc: OnboardingViewMvc,
    private val mFragmentManager: FragmentManager,
    private val mSettings: Settings
): OnboardingGetStartedViewMvc.Listener,
    OnboardingHowsTheAirViewMvc.Listener,
    OnboardingMeasureAndMapViewMvc.Listener,
    OnboardingYourPrivacyViewMvc.Listener {
    private val wizardNavigator = OnboardingWizardNavigator(mViewMvc, mFragmentManager)

    fun onBackPressed() {
        wizardNavigator.onBackPressed()

        when {
            mContextActivity.supportFragmentManager.fragments.last() is OnboardingMeasureAndMapFragment -> {
                mViewMvc.changeProgressBarColorToGreen()
            }
            mContextActivity.supportFragmentManager.fragments.last() is OnboardingGetStartedFragment -> {
                mViewMvc.hideProgressBar()
            }
            else -> {
                mViewMvc.changeProgressBarColorToBlue()
            }
        }
    }

    fun onCreate() {
        wizardNavigator.setupProgressBarMax()
        wizardNavigator.goToGetStarted(this)
        mViewMvc.hideProgressBar()
    }

    override fun onGetStartedClicked() {
        wizardNavigator.goToHowIsTheAir(this)
        mViewMvc.showProgressBar()
    }

    override fun onContinueHowsTheAirClicked() {
        wizardNavigator.goToMeasureAndMap(this)
        mViewMvc.changeProgressBarColorToGreen()
    }

    override fun onContinueMeasureAndMapClicked() {
        wizardNavigator.goToYourPrivacy(this)
        mViewMvc.changeProgressBarColorToBlue()
    }

    override fun onLearnMoreMeasureAndMapClicked() {
        val bottomsheet = LearnMoreMeasureAndMapBottomSheet()
        bottomsheet.show(mFragmentManager)
    }

    override fun onAcceptClicked() {
        CreateAccountActivity.start(mContextActivity, true)

        if(!mSettings.onboardingDisplayed()) {
            mSettings.onboardingAccepted()
        }
    }

    override fun onLearnMoreYourPrivacyClicked() {
        val bottomsheet = LearnMoreYourPrivacyBottomSheet()
        bottomsheet.show(mFragmentManager)
    }

}

package io.lunarlogic.aircasting.screens.onboarding

import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.create_account.CreateAccountViewMvc
import io.lunarlogic.aircasting.screens.new_session.NewSessionWizardNavigator
import io.lunarlogic.aircasting.screens.onboarding.page1.OnboardingPage1Fragment
import io.lunarlogic.aircasting.screens.onboarding.page1.OnboardingPage1ViewMvc
import io.lunarlogic.aircasting.screens.onboarding.page2.OnboardingPage2Fragment
import io.lunarlogic.aircasting.screens.onboarding.page2.OnboardingPage2ViewMvc
import io.lunarlogic.aircasting.screens.onboarding.page3.OnboardingPage3Fragment
import io.lunarlogic.aircasting.screens.onboarding.page3.OnboardingPage3ViewMvc
import io.lunarlogic.aircasting.screens.onboarding.page4.OnboardingPage4Fragment
import io.lunarlogic.aircasting.screens.onboarding.page4.OnboardingPage4ViewMvc

class OnboardingWizardNavigator(
    private val mViewMvc: OnboardingViewMvc,
    private val mFragmentManager: FragmentManager
) {
    interface BackPressedListener {
        fun onBackPressed()
    }

    private val STEP_PROGRESS = 15
    private var currentProgressStep = 0
    private var backPressedListener: BackPressedListener? = null

    fun goToPage1(listener: OnboardingPage1ViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingPage1Fragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToPage2(listener: OnboardingPage2ViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingPage2Fragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToPage3(listener: OnboardingPage3ViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingPage3Fragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToPage4(listener: OnboardingPage4ViewMvc.Listener) {
        incrementStepProgress()
        val fragment = OnboardingPage4Fragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    private fun registerBackPressed(listener: OnboardingWizardNavigator.BackPressedListener) {
        backPressedListener = listener
    }

    fun onBackPressed() {
        decrementStepProgress()
        backPressedListener?.onBackPressed()
    }

    private fun incrementStepProgress() {
        currentProgressStep += 1
        updateProgressBarView()
    }

    private fun decrementStepProgress() {
        currentProgressStep -= 1
        updateProgressBarView()
    }

    private fun updateProgressBarView() {
        val progressBar = mViewMvc.rootView?.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar?.progress = currentProgressStep * STEP_PROGRESS
    }

    private fun goToFragment(fragment: Fragment) {
        val fragmentTransaction = mFragmentManager.beginTransaction()
        val container = R.id.onboarding_fragment_container

        fragmentTransaction.replace(container, fragment)
        if (currentProgressStep > 1) {
            fragmentTransaction.addToBackStack(null)
        }

        fragmentTransaction.commit()
    }
}

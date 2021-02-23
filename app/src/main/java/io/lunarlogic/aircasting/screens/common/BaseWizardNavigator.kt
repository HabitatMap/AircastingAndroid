package io.lunarlogic.aircasting.screens.common

import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R

open class BaseWizardNavigator(
    private val mViewMvc: ViewMvc,
    private val mFragmentManager: FragmentManager,
    private val container: Int
) {
    interface BackPressedListener {
        fun onBackPressed()
    }

    open val STEP_PROGRESS = 15
    open var currentProgressStep = 0
    open var backPressedListener: BackPressedListener? = null

    protected fun registerBackPressed(listener: BackPressedListener) {
        backPressedListener = listener
    }

    fun onBackPressed() {
        decrementStepProgress()
        backPressedListener?.onBackPressed()
    }

    protected fun incrementStepProgress() {
        currentProgressStep += 1
        updateProgressBarView()
    }

    protected fun decrementStepProgress() {
        currentProgressStep -= 1
        updateProgressBarView()
    }

    protected fun updateProgressBarView() {
        val progressBar = mViewMvc.rootView?.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar?.progress = currentProgressStep * STEP_PROGRESS
    }

    protected fun goToFragment(fragment: Fragment) {
        val fragmentTransaction = mFragmentManager.beginTransaction()

        fragmentTransaction.replace(container, fragment)
        if (currentProgressStep > 1) {
            fragmentTransaction.addToBackStack(null)
        }

        fragmentTransaction.commit()
    }
}

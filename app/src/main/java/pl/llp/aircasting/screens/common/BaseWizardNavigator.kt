package pl.llp.aircasting.screens.common

import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.ProgressBarCounter

abstract class BaseWizardNavigator(
    private val mViewMvc: ViewMvc,
    private val mFragmentManager: FragmentManager,
    private val container: Int
) {
    interface BackPressedListener {
        fun onBackPressed()
    }

    protected abstract val STEP_PROGRESS: Int
    val progressBarCounter = ProgressBarCounter()
    private var currentProgressStep = 0
    private var backPressedListener: BackPressedListener? = null

    protected fun registerBackPressed(listener: BackPressedListener) {
        backPressedListener = listener
    }

    protected fun unregisterBackPressedListener() {
        backPressedListener = null
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
        progressBar?.max = progressBarCounter.currentProgressMax
    }

    open fun setupProgressBarMax(locationServicesAreOff: Boolean, areMapsDisabled: Boolean, isBluetoothDisabled: Boolean) {
        if (locationServicesAreOff) {
            progressBarCounter.increaseMaxProgress() // 1 additional step in flow
        }
        if (areMapsDisabled) {
            progressBarCounter.increaseMaxProgress() // 1 additional step in flow
        }
        if (isBluetoothDisabled) {
            progressBarCounter.increaseMaxProgress() // 1 additional step in flow
        }
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

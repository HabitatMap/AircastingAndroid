package pl.llp.aircasting.ui.view.screens.onboarding

import pl.llp.aircasting.ui.view.common.ViewMvc

interface OnboardingViewMvc: ViewMvc {
    fun changeProgressBarColorToGreen()
    fun changeProgressBarColorToBlue()
    fun hideProgressBar()
    fun showProgressBar()
}

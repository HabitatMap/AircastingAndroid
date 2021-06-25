package pl.llp.aircasting.screens.onboarding

import pl.llp.aircasting.screens.common.ViewMvc

interface OnboardingViewMvc: ViewMvc {
    fun changeProgressBarColorToGreen()
    fun changeProgressBarColorToBlue()
    fun hideProgressBar()
    fun showProgressBar()
}

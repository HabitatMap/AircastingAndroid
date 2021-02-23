package io.lunarlogic.aircasting.screens.onboarding

import io.lunarlogic.aircasting.screens.common.ViewMvc

interface OnboardingViewMvc: ViewMvc {
    fun changeProgressBarColorToGreen()
    fun changeProgressBarColorToBlue()
    fun hideProgressBar()
    fun showProgressBar()
}

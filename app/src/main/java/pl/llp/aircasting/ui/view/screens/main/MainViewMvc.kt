package pl.llp.aircasting.ui.view.screens.main

import pl.llp.aircasting.ui.view.common.ViewMvc

interface MainViewMvc: ViewMvc {
    fun showLoader()
    fun hideLoader()
    fun navigateToAppropriateTab()
}

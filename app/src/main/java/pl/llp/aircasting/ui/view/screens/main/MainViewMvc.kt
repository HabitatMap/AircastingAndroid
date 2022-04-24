package pl.llp.aircasting.ui.view.screens.main

import pl.llp.aircasting.ui.view.screens.common.ViewMvc

interface MainViewMvc: ViewMvc {
    fun showLoader()
    fun hideLoader()
}

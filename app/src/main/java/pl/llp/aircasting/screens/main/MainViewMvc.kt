package pl.llp.aircasting.screens.main

import pl.llp.aircasting.screens.common.ViewMvc

interface MainViewMvc: ViewMvc {
    fun showLoader()
    fun hideLoader()
}

package pl.llp.aircasting.screens.main

import pl.llp.aircasting.screens.common.ViewMvc

interface MainViewMvc: ViewMvc {
    interface Listener {
        fun onFinishedReorderingButtonClicked()
    }
    fun showLoader()
    fun hideLoader()
    fun showAppBarMenu()
    fun hideAppBarMenu()
    fun showReorderingFinishedButton()
    fun hideReorderingFinishedButton()
}

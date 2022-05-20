package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.ui.view.common.ViewMvc

interface DashboardViewMvc : ViewMvc {
    fun goToTab(tabId: Int)
}

package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.ui.view.screens.common.ViewMvc

interface DashboardViewMvc : ViewMvc {
    fun goToTab(tabId: Int)
}

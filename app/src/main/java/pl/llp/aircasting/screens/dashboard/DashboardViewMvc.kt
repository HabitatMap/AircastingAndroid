package pl.llp.aircasting.screens.dashboard

import pl.llp.aircasting.screens.common.ViewMvc

interface DashboardViewMvc : ViewMvc {
    fun goToTab(tabId: Int)
}

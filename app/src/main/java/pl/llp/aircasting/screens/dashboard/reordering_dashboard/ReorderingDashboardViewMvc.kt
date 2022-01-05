package pl.llp.aircasting.screens.dashboard.reordering_dashboard

import pl.llp.aircasting.screens.common.ViewMvc

interface ReorderingDashboardViewMvc: ViewMvc {
    fun goToTab(tabId: Int)
}

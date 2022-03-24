package pl.llp.aircasting.screens.dashboard.reordering_dashboard

import pl.llp.aircasting.screens.common.BaseController
import pl.llp.aircasting.screens.dashboard.DashboardViewMvcImpl
import pl.llp.aircasting.screens.dashboard.SessionsTab

class ReorderingDashboardController(
    private val viewMvc: DashboardViewMvcImpl?
) : BaseController<DashboardViewMvcImpl>(viewMvc) {

    fun onCreate(tabId: Int?) {
        viewMvc?.goToTab(tabId ?: SessionsTab.FOLLOWING.value)
    }
}


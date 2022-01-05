package pl.llp.aircasting.screens.dashboard.reordering_dashboard

import pl.llp.aircasting.screens.common.BaseController
import pl.llp.aircasting.screens.dashboard.DashboardViewMvcImpl

class ReorderingDashboardController(
    private val viewMvc: DashboardViewMvcImpl?
) : BaseController<DashboardViewMvcImpl>(viewMvc) {

    fun onCreate(tabId: Int?) {
        viewMvc?.goToTab(tabId ?: 0)
    }
}


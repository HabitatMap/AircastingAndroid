package pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard

import pl.llp.aircasting.ui.view.common.BaseController
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardViewMvcImpl
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab

class ReorderingDashboardController(
    private val viewMvc: DashboardViewMvcImpl?
) : BaseController<DashboardViewMvcImpl>(viewMvc) {

    fun onCreate(tabId: Int?) {
        viewMvc?.goToTab(tabId ?: SessionsTab.FOLLOWING.value)
    }
}


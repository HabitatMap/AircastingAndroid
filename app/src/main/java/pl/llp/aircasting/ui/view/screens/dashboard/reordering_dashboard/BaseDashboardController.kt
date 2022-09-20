package pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard

import pl.llp.aircasting.ui.view.common.BaseController
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardViewMvcImpl
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab

open class BaseDashboardController(
    private val viewMvc: DashboardViewMvcImpl?
) : BaseController<DashboardViewMvcImpl>(viewMvc) {

    open fun onCreate(tabId: Int?) {
        viewMvc?.setup()
        viewMvc?.goToTab(tabId ?: SessionsTab.FOLLOWING.value)
    }
}


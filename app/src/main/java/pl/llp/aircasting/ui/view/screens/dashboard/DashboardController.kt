package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.ui.view.screens.common.BaseController

class DashboardController(
    private val viewMvc: DashboardViewMvcImpl?
) : BaseController<DashboardViewMvcImpl>(viewMvc) {

    fun onCreate(tabId: Int?) {
        viewMvc?.goToTab(tabId ?: SessionsTab.FOLLOWING.value)
    }
}

package pl.llp.aircasting.screens.dashboard

import pl.llp.aircasting.screens.common.BaseController

class DashboardController(
    private val viewMvc: DashboardViewMvcImpl?
) : BaseController<DashboardViewMvcImpl>(viewMvc) {

    fun onCreate(tabId: Int?) {
        viewMvc?.goToTab(tabId ?: SessionsTab.FOLLOWING.value)
    }
}

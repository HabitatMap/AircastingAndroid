package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.ui.view.common.BaseController

class DashboardController(
    private val viewMvc: DashboardViewMvcImpl?,
    private val sessionsSyncService: SessionsSyncService
) : BaseController<DashboardViewMvcImpl>(viewMvc), DashboardViewMvc.Listener {

    fun onCreate(tabId: Int?) {
        viewMvc?.goToTab(tabId ?: SessionsTab.FOLLOWING.value)
        viewMvc?.registerListener(this)
    }

    override fun onSwipeToRefreshTriggered() {
        sessionsSyncService.sync(
            { mViewMvc?.showLoader() },
            { mViewMvc?.hideLoader() }
        )
    }
}
